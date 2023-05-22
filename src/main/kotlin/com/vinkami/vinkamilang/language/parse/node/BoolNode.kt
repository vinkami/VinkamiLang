package com.vinkami.vinkamilang.language.parse.node

import com.vinkami.vinkamilang.language.lex.Token

class BoolNode(val token: Token): BaseNode(token.startPos, token.endPos) {
    override fun toString(): String {
        return token.toString()
    }
}