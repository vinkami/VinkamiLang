package com.vinkami.vinkamilang.language.interpret.`object`

import com.vinkami.vinkamilang.language.interpret.Referables
import com.vinkami.vinkamilang.language.lex.Position

class BoolObj(override val value: Boolean, override val startPos: Position, override val endPos: Position): BaseObject {
    override val type = "Boolean"
    override val property = Referables(null, isRoot=false)
    override val boolVal
        get() = value

    override fun toString(): String {
        return if (value) "true" else "false"
    }
}