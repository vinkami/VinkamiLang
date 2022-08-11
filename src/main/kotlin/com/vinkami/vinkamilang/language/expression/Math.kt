package com.vinkami.vinkamilang.language.expression

import com.vinkami.vinkamilang.language.Token
import com.vinkami.vinkamilang.language.position.ParsingPosition

class Math(val op: Token, val lhs: Expression, val rhs: Expression, pos: ParsingPosition): Expression(pos) {

    override fun toString(): String {
        return "($lhs $op $rhs)"
    }
}