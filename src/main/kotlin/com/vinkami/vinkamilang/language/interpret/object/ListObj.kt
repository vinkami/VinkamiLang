package com.vinkami.vinkamilang.language.interpret.`object`

import com.vinkami.vinkamilang.language.interpret.Referables
import com.vinkami.vinkamilang.language.lex.Position

class ListObj(val elements: List<BaseObject>, override val startPos: Position, override val endPos: Position): BaseObject {
    override val type = "List"
    override val value = elements.joinToString(", ", "[", "]")
    override val property = Referables(null, isRoot=false)
    override fun boolVal() = elements.isNotEmpty()
    override fun toString() = value
}