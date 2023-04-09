package com.vinkami.vinkamilang.language.interpret.`object`

import com.vinkami.vinkamilang.language.interpret.Referables
import com.vinkami.vinkamilang.language.lex.Position

class CustomObj(override val type: String, val ref: Referables, override val startPos: Position, override val endPos: Position): BaseObject {
    override val value = "<Object $type>"

    override fun toString(): String = "<Object $type>"
}