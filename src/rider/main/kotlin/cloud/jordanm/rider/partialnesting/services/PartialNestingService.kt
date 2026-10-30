package cloud.jordanm.rider.partialnesting.services

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.jetbrains.rd.framework.IProtocol
import com.jetbrains.rd.util.lifetime.LifetimeDefinition
import com.jetbrains.rider.projectView.ProjectModelViewUpdater
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.projectView.workspace.ProjectModelEntity
import com.jetbrains.rider.projectView.workspace.ProjectModelEntityReference
import com.jetbrains.rider.projectView.workspace.toReference
import cloud.jordanm.rider.partialnesting.protocol.FolderRequest
import cloud.jordanm.rider.partialnesting.protocol.NestingGroup
import cloud.jordanm.rider.partialnesting.protocol.PartialNestingModel
import cloud.jordanm.rider.partialnesting.protocol.partialNestingModel
import cloud.jordanm.rider.partialnesting.settings.PartialNestingSettings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

@Service(Service.Level.PROJECT)
class PartialNestingService(private val project: Project, private val scope: CoroutineScope) : Disposable {
    private data class FolderKey(val parent: ProjectModelEntityReference, val request: FolderRequest)
    private class FolderState {
        @Volatile var groups: List<NestingGroup> = emptyList()
        var job: Job? = null
    }

    private val lifetimeDefinition = LifetimeDefinition()
    private val folders = ConcurrentHashMap<FolderKey, FolderState>()
    private var refresh: Job? = null
    private var connectedProtocol: IProtocol? = null
    private var model: PartialNestingModel? = null

    init {
        val connection = project.messageBus.connect(this)
        scope.launch {
            WorkspaceModel.getInstance(project).eventLog.collect { event ->
                if (event.getChanges(ProjectModelEntity::class.java).any()) invalidate()
            }
        }
        connection.subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(events: List<VFileEvent>) {
                if (events.any {
                    it.path.endsWith(".cs", ignoreCase = true) || it.path.endsWith(".csproj", ignoreCase = true)
                }) invalidate()
            }
        })
    }

    fun getGroups(parent: ProjectModelEntity, projectPath: String, paths: Set<String>): List<NestingGroup> {
        if (!project.service<PartialNestingSettings>().state.enabled) return emptyList()

        val key = FolderKey(parent.toReference(), FolderRequest(projectPath, paths.sorted()))
        val state = folders.computeIfAbsent(key) { FolderState() }
        synchronized(state) {
            if (state.job == null) {
                state.job = scope.launch(Dispatchers.EDT, start = CoroutineStart.LAZY) { load(key, state) }
            }
            state.job!!.start()
        }
        return state.groups
    }

    fun invalidate(path: String = "") {
        scope.launch(Dispatchers.EDT) {
            folders.entries.removeIf { (key, state) ->
                val affected = path.isEmpty() || path in key.request.filePaths
                if (affected) state.job?.cancel()
                affected
            }
            refresh?.cancel()
            refresh = scope.launch(Dispatchers.EDT) {
                delay(250)
                ProjectModelViewUpdater.fireUpdate(project) { it.updateAll() }
            }
        }
    }

    private suspend fun load(key: FolderKey, state: FolderState) {
        try {
            val connection = getModel() ?: return
            val groups = connection.getGroups.startSuspending(lifetimeDefinition.lifetime, key.request)
            if (folders[key] !== state) return

            state.groups = groups
            val parent = readAction { key.parent.getEntity(project) } ?: return
            ProjectModelViewUpdater.fireUpdate(project) { it.updateWithChildren(parent) }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            logger<PartialNestingService>().warn("Could not load partial-class nesting", exception)
        }
    }

    private fun getModel(): PartialNestingModel? {
        val solution = project.solution
        val protocol = solution.protocol ?: return null
        if (protocol === connectedProtocol) return model

        connectedProtocol = protocol
        return protocol.partialNestingModel.also { connection ->
            model = connection
            connection.invalidated.advise(lifetimeDefinition.lifetime, ::invalidate)
            solution.isLoaded.advise(lifetimeDefinition.lifetime) { if (it) invalidate() }
        }
    }

    override fun dispose() {
        lifetimeDefinition.terminate()
    }
}
