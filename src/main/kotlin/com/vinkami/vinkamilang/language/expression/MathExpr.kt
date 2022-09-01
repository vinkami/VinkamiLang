package com.vinkami.vinkamilang.language.expression

import com.vinkami.vinkamilang.language.Token
import com.vinkami.vinkamilang.language.position.ParsingPosition

class MathExpr(val op: Token, val lhs: BaseExpression, val rhs: BaseExpression, pos: ParsingPosition): BaseExpression(pos) {
    override fun toString(): String {
        return "($lhs $op $rhs)"
    }
}