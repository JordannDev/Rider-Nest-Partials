package com.jordanmolenaar.rider.partialnesting.explorer

import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.rider.projectView.views.actions.ConfigureFilesNestingAction
import com.jetbrains.rider.projectView.views.solutionExplorer.SolutionExplorerCustomization
import com.jetbrains.rider.projectView.views.solutionExplorer.SolutionExplorerViewSettings
import com.jetbrains.rider.projectView.views.solutionExplorer.nodes.SolutionExplorerModelNode
import com.jetbrains.rider.projectView.workspace.ProjectModelEntity
import com.jetbrains.rider.projectView.workspace.containingProjectEntity
import com.jetbrains.rider.projectView.workspace.getFile
import com.jetbrains.rider.projectView.workspace.isProjectFile
import com.jordanmolenaar.rider.partialnesting.services.PartialNestingService

class PartialNestingCustomization(project: Project) : SolutionExplorerCustomization(project) {
    override fun modifyChildren(
        entity: ProjectModelEntity,
        settings: SolutionExplorerViewSettings,
        children: MutableList<AbstractTreeNode<*>>,
    ) {
        if (entity.isProjectFile() || !ConfigureFilesNestingAction.useFileNestingRules()) return

        val files = children.filterIsInstance<SolutionExplorerModelNode>()
            .filter { node ->
                node.entity?.isProjectFile() == true && node.virtualFile?.extension.equals("cs", ignoreCase = true)
            }
        if (files.size < 2) return

        val projectPath = entity.containingProjectEntity()?.getFile()?.path ?: return
        val filesByPath = files.associateBy { it.virtualFile!!.path }
        val groups = project.service<PartialNestingService>().getGroups(entity, projectPath, filesByPath.keys)

        for (group in groups) {
            val primary = filesByPath[group.parentPath] ?: continue
            val parts = group.childPaths.mapNotNull(filesByPath::get)
            if (parts.isEmpty()) continue

            val index = children.indexOf(primary)
            if (index < 0 || !children.containsAll(parts)) continue

            children[index] = PartialClassNode(project, primary, parts)
            children.removeAll(parts.toSet())
        }
    }
}
