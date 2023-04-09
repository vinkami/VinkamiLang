package com.vinkami.vinkamilang.language.interpret.`object`

import com.vinkami.vinkamilang.language.parse.node.ClassNode

class ClassObj(val node: ClassNode): BaseObject {
    override val type = "Class"
    override val value = node.name
    override val startPos = node.startPos
    override val endPos = node.endPos

    override fun toString(): String {
        return "<class $value>"
    }
}