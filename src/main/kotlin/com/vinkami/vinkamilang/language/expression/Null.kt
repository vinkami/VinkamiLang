package com.vinkami.vinkamilang.language.expression

import com.vinkami.vinkamilang.language.position.ParsingPosition

class Null: Expression(ParsingPosition.dummy) {
    override fun toString(): String {
        return "null"
    }
}
