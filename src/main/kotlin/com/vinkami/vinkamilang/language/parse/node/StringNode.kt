package com.vinkami.vinkamilang.language.parse.node

import com.vinkami.vinkamilang.language.lex.Token

class StringNode(token: Token): BaseNode(token.startPos, token.endPos) {
    val value = token.value  // no need to do substring because the lexer already did it
    val quote = token.value[0]

    override fun toString(): String {
        return "$quote$value$quote"
    }
}