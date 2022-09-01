package com.vinkami.vinkamilang.language.expression

import com.vinkami.vinkamilang.language.Token
import com.vinkami.vinkamilang.language.position.ParsingPosition

class NumberExpr(val value: Token, pos: ParsingPosition): BaseExpression(pos) {
    override fun toString(): String {
        return value.toString()
    }
}
