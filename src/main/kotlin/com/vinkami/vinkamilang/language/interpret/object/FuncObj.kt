package com.vinkami.vinkamilang.language.interpret.`object`

import com.vinkami.vinkamilang.language.lex.Position

class FuncObj(val name: String, val params: List<String>, val body: BaseObject, override val startPos: Position, override val endPos: Position): BaseObject {
    override val value = name

    override fun toString(): String {
        return "<function $name>"
    }
}