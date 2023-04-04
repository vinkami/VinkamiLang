package com.vinkami.vinkamilang.language.interpret.`object`

import com.vinkami.vinkamilang.language.lex.Position

class NullObj(override val startPos: Position, override val endPos: Position): BaseObject {
    override val type = ObjectType.NULL
    override val value = "null"
    override fun boolVal() = false
    override fun toString(): String {
        return "null"
    }
}