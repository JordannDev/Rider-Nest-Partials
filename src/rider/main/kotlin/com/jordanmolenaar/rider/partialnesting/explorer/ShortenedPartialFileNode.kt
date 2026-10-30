package com.jordanmolenaar.rider.partialnesting.explorer

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.PresentableNodeDescriptor.ColoredFragment
import com.intellij.openapi.project.Project
import com.intellij.xml.util.XmlStringUtil
import com.jetbrains.rider.projectView.views.solutionExplorer.nodes.SolutionExplorerModelNode

internal class ShortenedPartialFileNode(
    project: Project,
    original: SolutionExplorerModelNode,
    private val fullName: String,
    private val label: String,
) : SolutionExplorerModelNode(project, original.entityReference, original.settings) {
    override fun update(presentation: PresentationData) {
        super.update(presentation)
        if (name != fullName) return

        val fragments = presentation.coloredText.toList()
        val filenameIndex = fragments.indexOfFirst { it.text == fullName }
        if (filenameIndex < 0) return

        presentation.clearText()
        fragments.forEachIndexed { index, fragment ->
            presentation.addText(if (index == filenameIndex) {
                ColoredFragment(label, fragment.toolTip, fragment.attributes)
            } else fragment)
        }
        presentation.presentableText = label

        val tooltip = presentation.tooltip
        presentation.tooltip = when {
            tooltip.isNullOrBlank() -> fullName
            tooltip.contains(fullName) -> tooltip
            tooltip.startsWith("<html>") ->
                "<html>" + XmlStringUtil.escapeString(fullName) + "<br>" + tooltip.removePrefix("<html>")
            else -> "$fullName\n$tooltip"
        }
    }
}
