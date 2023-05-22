package com.vinkami.vinkamilang.language.interpret.`object`

import com.vinkami.vinkamilang.language.interpret.Referables
import com.vinkami.vinkamilang.language.lex.Position

class DictObj(val elements: Map<BaseObject, BaseObject>, override val startPos: Position, override val endPos: Position): BaseObject {
    override val type = "Dict"
    override val value = elements.entries.joinToString(", ", "[", "]") { "${it.key}: ${it.value}" }
    override val property = Referables(null, isRoot=false)
    override fun boolVal() = elements.isNotEmpty()
    override fun toString() = value
}