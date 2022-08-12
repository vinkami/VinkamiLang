package com.vinkami.vinkamilang.language.expression

import com.vinkami.vinkamilang.language.Token
import com.vinkami.vinkamilang.language.position.ParsingPosition

class Bracket(val expr: Expression, val bracL: Token, val bracR: Token, pos: ParsingPosition): Expression(pos) {
    override fun toString(): String {
        return "($bracL $expr $bracR)"
    }

//    override fun interpret(): Any {
//        return expr.interpret()
//    }
}