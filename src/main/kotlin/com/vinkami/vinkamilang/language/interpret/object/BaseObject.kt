package com.vinkami.vinkamilang.language.interpret.`object`

import com.vinkami.vinkamilang.language.exception.SyntaxError
import com.vinkami.vinkamilang.language.lex.Position

interface BaseObject {
    val value: Any
    val startPos: Position
    val endPos: Position
    fun boolVal(): Boolean = true

    operator fun plus(other: BaseObject): BaseObject = throw SyntaxError("Can't add $this and $other", startPos, endPos)
    operator fun minus(other: BaseObject): BaseObject = throw SyntaxError("Can't subtract $this by $other", startPos, endPos)
    operator fun times(other: BaseObject): BaseObject = throw SyntaxError("Can't multiply $this and $other", startPos, endPos)
    operator fun div(other: BaseObject): BaseObject = throw SyntaxError("Can't divide $this by $other", startPos, endPos)
    operator fun rem(other: BaseObject): BaseObject = throw SyntaxError("Can't get remainder of $this divided by $other", startPos, endPos)
    fun power(other: BaseObject): BaseObject = throw SyntaxError("Can't get $this to the power of $other", startPos, endPos)

    operator fun unaryPlus(): BaseObject = throw SyntaxError("Can't apply unary plus to $this", startPos, endPos)
    operator fun unaryMinus(): BaseObject = throw SyntaxError("Can't apply unary minus to $this", startPos, endPos)
}