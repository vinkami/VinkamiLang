package com.vinkami.vinkamilang.language.interpret.`object`

import com.vinkami.vinkamilang.language.interpret.Referables
import com.vinkami.vinkamilang.language.lex.Position
import kotlin.math.round

class StringObj(override val value: String, override val startPos: Position, override val endPos: Position): BaseObject {
    override val type = "String"
    override val property = Referables(null, isRoot=false)
    override val boolVal
        get() = value.isNotEmpty()
    override fun toString() = value

    override fun plus(other: BaseObject): BaseObject {
        if (other is StringObj) return StringObj(value + other.value, startPos, other.endPos)
        return super.plus(other)
    }

    override fun times(other: BaseObject): BaseObject {
        if (other is NumberObj && round(other.value) == other.value) return StringObj(value.repeat(other.value.toInt()), startPos, other.endPos)
        return super.times(other)
    }
}