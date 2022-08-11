package com.vinkami.vinkamilang.language.expression

import com.vinkami.vinkamilang.language.Token
import com.vinkami.vinkamilang.language.position.ParsingPosition

class Number(val value: Token, pos: ParsingPosition): Expression(pos) {
    override fun toString(): String {
        return value.toString()
    }
}
