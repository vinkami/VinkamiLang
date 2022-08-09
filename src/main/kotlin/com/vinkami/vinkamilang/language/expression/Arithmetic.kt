package com.vinkami.vinkamilang.language.expression

import com.vinkami.vinkamilang.language.Token

class Arithmetic(op: Token, val lhs: Expression, val rhs: Expression): Expression(op) {
    val op = value

    override fun toString(): String {
        return "($lhs $op $rhs)"
    }
}