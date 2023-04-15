package com.vinkami.vinkamilang.language.parse.node

import com.vinkami.vinkamilang.language.lex.Token

class StringNode(token: Token): BaseNode(token.startPos, token.endPos) {
    val value = token.value  // no need to do substring because the lexer already did it

    override fun toString(): String {
        return "\"$value\""
    }
}