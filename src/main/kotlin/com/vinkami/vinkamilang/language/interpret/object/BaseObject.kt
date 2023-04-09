package com.vinkami.vinkamilang.language.interpret.`object`

import com.vinkami.vinkamilang.language.exception.SyntaxError
import com.vinkami.vinkamilang.language.lex.Position

interface BaseObject {
    val type: String
    val value: Any
    val startPos: Position
    val endPos: Position
    fun boolVal(): Boolean = true

    fun plus(other: BaseObject): BaseObject = throw SyntaxError("Can't add $this and $other", startPos, endPos)
    fun minus(other: BaseObject): BaseObject = throw SyntaxError("Can't subtract $this by $other", startPos, endPos)
    fun times(other: BaseObject): BaseObject = throw SyntaxError("Can't multiply $this and $other", startPos, endPos)
    fun divide(other: BaseObject): BaseObject = throw SyntaxError("Can't divide $this by $other", startPos, endPos)
    fun mod(other: BaseObject): BaseObject = throw SyntaxError("Can't get remainder of $this divided by $other", startPos, endPos)
    fun power(other: BaseObject): BaseObject = throw SyntaxError("Can't get $this to the power of $other", startPos, endPos)

    fun unaryPlus(): BaseObject = throw SyntaxError("Can't apply unary plus to $this", startPos, endPos)
    fun unaryMinus(): BaseObject = throw SyntaxError("Can't apply unary minus to $this", startPos, endPos)

    fun equal(other: BaseObject): BaseObject = BoolObj(value == other.value, startPos, other.endPos)
    fun notEqual(other: BaseObject): BaseObject = BoolObj(value != other.value, startPos, other.endPos)
    fun lessEqual(other: BaseObject): BaseObject = throw SyntaxError("Can't compare $this and $other", startPos, other.endPos)
    fun greaterEqual(other: BaseObject): BaseObject = throw SyntaxError("Can't compare $this and $other", startPos, other.endPos)
    fun less(other: BaseObject): BaseObject = throw SyntaxError("Can't compare $this and $other", startPos, other.endPos)
    fun greater(other: BaseObject): BaseObject = throw SyntaxError("Can't compare $this and $other", startPos, other.endPos)
}
