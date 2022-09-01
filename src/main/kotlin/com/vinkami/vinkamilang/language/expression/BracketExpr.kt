package com.vinkami.vinkamilang.language.expression

import com.vinkami.vinkamilang.language.Token
import com.vinkami.vinkamilang.language.position.ParsingPosition

class BracketExpr(val expr: BaseExpression, val bracL: Token, val bracR: Token, pos: ParsingPosition): BaseExpression(pos) {
    override fun toString(): String {
        return "($bracL $expr $bracR)"
    }

//    override fun interpret(): Any {
//        return expr.interpret()
//    }
}