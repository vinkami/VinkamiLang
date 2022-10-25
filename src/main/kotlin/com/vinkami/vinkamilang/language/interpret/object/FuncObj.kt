package com.vinkami.vinkamilang.language.interpret.`object`

import com.vinkami.vinkamilang.language.lex.Position
import com.vinkami.vinkamilang.language.parse.node.BaseNode

class FuncObj(val name: String, val params: Map<String, String>, val body: BaseNode, override val startPos: Position, override val endPos: Position): BaseObject {
    // params: Map<paramName, paramType>
    override val value = name

    override fun toString(): String {
        return "<function $name>"
    }
}