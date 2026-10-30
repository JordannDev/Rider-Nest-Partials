package cloud.jordanm.rider.partialnesting.settings

import com.intellij.openapi.components.service
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.selected
import cloud.jordanm.rider.partialnesting.services.PartialNestingService
import javax.swing.JCheckBox

class PartialNestingConfigurable(private val project: Project) : BoundConfigurable("Partial Class Nesting") {
    override fun createPanel() = panel {
        val settings = project.service<PartialNestingSettings>().state
        lateinit var nesting: Cell<JCheckBox>

        row {
            nesting = checkBox("Nest partial-class files in Solution Explorer")
                .bindSelected(settings::enabled)
        }
        indent {
            row {
                checkBox("Shorten nested partial filenames")
                    .bindSelected(settings::shortenNestedFilenames)
                    .enabledIf(nesting.selected)
                    .comment(
                        "Shows shorter labels for partial-class files nested by this plugin. " +
                            "Removes the parent filename without its extension, followed by a dot, " +
                            "from the start of each child filename. Matching is case-sensitive.<br><br>" +
                            "For example, beneath BatchRepository.cs, BatchRepository.Inventory.cs " +
                            "appears as Inventory.cs, and BatchRepository.Inventory.Queries.cs " +
                            "appears as Inventory.Queries.cs.<br><br>" +
                            "Files without that prefix keep their full names. If shortening would " +
                            "give two children the same label, their full names are kept. " +
                            "Rider's existing nested children, such as Designer.cs files, are unchanged.<br><br>" +
                            "Only Solution Explorer labels change. Full filenames remain in tooltips; " +
                            "files, paths, sorting, navigation, and file actions stay unchanged. " +
                            "This setting is saved per project, is off by default, and requires " +
                            "partial-class nesting to be enabled. Changes take effect when you save settings.",
                    )
            }
        }
    }

    override fun apply() {
        super.apply()
        project.service<PartialNestingService>().invalidate()
    }
}
