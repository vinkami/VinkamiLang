package com.vinkami.vinkamilang.language.interpret.`object`

import com.vinkami.vinkamilang.language.lex.Position

class BoolObj(override val value: Boolean, override val startPos: Position, override val endPos: Position): BaseObject {
    override fun toString(): String {
        return if (value) "true" else "false"
    }
}