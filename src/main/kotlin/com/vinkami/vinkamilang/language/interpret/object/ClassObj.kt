package com.vinkami.vinkamilang.language.interpret.`object`

import com.vinkami.vinkamilang.language.interpret.Referables
import com.vinkami.vinkamilang.language.parse.node.ClassNode

class ClassObj(val node: ClassNode): BaseObject {
    override val type = "Class"
    override val value = node.name.value
    override val startPos = node.startPos
    override val endPos = node.endPos
    override val property = Referables(null, isRoot=false)

    override fun toString(): String {
        return "<class $value>"
    }
}