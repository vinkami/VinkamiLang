package com.vinkami.vinkamilang.language.interpret.`object`

import com.vinkami.vinkamilang.language.parse.node.FuncNode

class FuncObj(val node: FuncNode): BaseObject {
    override val value = node.name
    override val startPos = node.startPos
    override val endPos = node.endPos

    override fun toString(): String {
        return "<function $value>"
    }
}