package com.vinkami.vinkamilang.language.expression

import com.vinkami.vinkamilang.language.Token

class Bracket(expr: Expression, val bracL: Token, val bracR: Token): Expression(expr) {
    override fun toString(): String {
        return "($bracL $value $bracR)"
    }
}