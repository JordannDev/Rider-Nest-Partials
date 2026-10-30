package model

import com.jetbrains.rd.generator.nova.*
import com.jetbrains.rd.generator.nova.PredefinedType.*

object PartialNestingRoot : Root()

object PartialNestingModel : Ext(PartialNestingRoot) {
    private val folderRequest = structdef {
        field("projectPath", string)
        field("filePaths", immutableList(string))
    }

    private val nestingGroup = structdef {
        field("parentPath", string)
        field("childPaths", immutableList(string))
    }

    init {
        call("getGroups", folderRequest, immutableList(nestingGroup))
        signal("invalidated", string)
    }
}
