package com.vinkami.vinkamilang.language.parse.node

import com.vinkami.vinkamilang.language.lex.Token

class NumberNode(token: Token): BaseNode(token.startPos, token.endPos) {
    val value = token.value

    override fun toString(): String {
        return value
    }
}