package com.vinkami.vinkamilang.language.interpret.`object`

import com.vinkami.vinkamilang.language.exception.SyntaxError
import com.vinkami.vinkamilang.language.lex.Position
import kotlin.math.pow

class NumberObj(override val value: Float, override val startPos: Position, override val endPos: Position): BaseObject {
    override fun boolVal(): Boolean = value != 0f

    override fun toString(): String {
        return value.toString()
    }

    override operator fun plus(other: BaseObject): BaseObject {
        if (other is NumberObj) return NumberObj(value + other.value, startPos, other.endPos)
        return super.plus(other)
    }

    override operator fun minus(other: BaseObject): BaseObject {
        if (other is NumberObj) return NumberObj(value - other.value, startPos, other.endPos)
        return super.minus(other)
    }

    override operator fun times(other: BaseObject): BaseObject {
        if (other is NumberObj) return NumberObj(value * other.value, startPos, other.endPos)
        return super.times(other)
    }

    override operator fun div(other: BaseObject): BaseObject {
        if (other is NumberObj) return NumberObj(value / other.value, startPos, other.endPos)
        return super.div(other)
    }

    override fun power(other: BaseObject): BaseObject {
        if (other is NumberObj) return NumberObj(value.pow(other.value), startPos, other.endPos)
        return super.power(other)
    }

    override operator fun unaryPlus(): BaseObject {
        return NumberObj(+value, startPos, endPos)
    }

    override operator fun unaryMinus(): BaseObject {
        return NumberObj(-value, startPos, endPos)
    }

    override fun lessEqual(other: BaseObject): BaseObject {
        if (other !is NumberObj) throw SyntaxError("Can't compare $this and $other", startPos, other.endPos)
        return BoolObj(value <= other.value, startPos, other.endPos)
    }

    override fun greaterEqual(other: BaseObject): BaseObject {
        if (other !is NumberObj) throw SyntaxError("Can't compare $this and $other", startPos, other.endPos)
        return BoolObj(value >= other.value, startPos, other.endPos)
    }

    override fun less(other: BaseObject): BaseObject {
        if (other !is NumberObj) throw SyntaxError("Can't compare $this and $other", startPos, other.endPos)
        return BoolObj(value < other.value, startPos, other.endPos)
    }

    override fun greater(other: BaseObject): BaseObject {
        if (other !is NumberObj) throw SyntaxError("Can't compare $this and $other", startPos, other.endPos)
        return BoolObj(value > other.value, startPos, other.endPos)
    }
}