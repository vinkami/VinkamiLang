package com.vinkami.vinkamilang.language.interpret.`object`

import com.vinkami.vinkamilang.language.lex.Position

class NullObj(override val startPos: Position, override val endPos: Position): BaseObject {
    override val value = "null"
    override fun toString(): String {
        return "null"
    }
}