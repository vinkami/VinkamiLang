package com.vinkami.vinkamilang.language.interpret.`object`

import com.vinkami.vinkamilang.language.interpret.Referables
import com.vinkami.vinkamilang.language.lex.Position

class NullObj(override val startPos: Position, override val endPos: Position): BaseObject {
    override val type = "Null"
    override val value = "null"
    override val property = Referables(null, isRoot=false)
    override val boolVal = false
    override fun toString() = "null"
}