package com.vinkami.vinkamilang.language.parse.node

import com.vinkami.vinkamilang.language.lex.Token

class NullNode(token: Token): BaseNode(token.startPos, token.endPos) {
    override fun toString(): String {
        return "null"
    }
}