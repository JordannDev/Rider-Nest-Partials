package com.jordanmolenaar.rider.partialnesting.settings

import com.intellij.openapi.components.service
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.jordanmolenaar.rider.partialnesting.services.PartialNestingService

class PartialNestingConfigurable(private val project: Project) : BoundConfigurable("Partial Class Nesting") {
    override fun createPanel() = panel {
        row {
            checkBox("Nest partial-class files in Solution Explorer")
                .bindSelected(project.service<PartialNestingSettings>().state::enabled)
        }
    }

    override fun apply() {
        super.apply()
        project.service<PartialNestingService>().invalidate()
    }
}
