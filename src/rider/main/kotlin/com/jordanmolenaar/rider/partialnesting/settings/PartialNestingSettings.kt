package com.jordanmolenaar.rider.partialnesting.settings

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service(Service.Level.PROJECT)
@State(name = "PartialClassNesting", storages = [Storage("partial-class-nesting.xml")])
class PartialNestingSettings : SimplePersistentStateComponent<PartialNestingSettings.Settings>(Settings()) {
    class Settings : BaseState() {
        var enabled by property(true)
        var shortenNestedFilenames by property(false)
    }
}
