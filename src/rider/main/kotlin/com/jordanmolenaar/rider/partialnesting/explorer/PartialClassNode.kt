package com.jordanmolenaar.rider.partialnesting.explorer

import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.tree.LeafState
import com.jetbrains.rider.projectView.views.SolutionViewCustomEntityContainer
import com.jetbrains.rider.projectView.views.solutionExplorer.nodes.SolutionExplorerModelNode
import com.jetbrains.rider.projectView.workspace.ProjectModelEntityReference
import com.jetbrains.rider.projectView.workspace.isParentOf
import com.jordanmolenaar.rider.partialnesting.settings.PartialNestingSettings

internal class PartialClassNode(
    project: Project,
    original: SolutionExplorerModelNode,
    private val parts: List<SolutionExplorerModelNode>,
) : SolutionExplorerModelNode(project, original.entityReference, original.settings), SolutionViewCustomEntityContainer {
    override fun calculateChildren(): MutableList<AbstractTreeNode<*>> {
        val children = super.calculateChildren()
        if (!project.service<PartialNestingSettings>().state.shortenNestedFilenames) {
            return children.apply { addAll(parts) }
        }

        val names = parts.map { it.name }
        val labels = NestedFileLabels.forChildren(name, names, children.mapNotNull { it.name })
        parts.forEachIndexed { index, part ->
            children.add(if (labels[index] != names[index]) {
                ShortenedPartialFileNode(project, part, names[index], labels[index])
            } else part)
        }
        return children
    }

    override fun getLeafState(): LeafState = LeafState.NEVER

    override fun contains(file: VirtualFile): Boolean = super.contains(file) || parts.any { it.contains(file) }

    override fun contains(entity: ProjectModelEntityReference): Boolean {
        val target = entity.getEntity(project) ?: return false
        return parts.any { part ->
            part.entityReference == entity || part.entity?.isParentOf(target) == true
        }
    }
}
