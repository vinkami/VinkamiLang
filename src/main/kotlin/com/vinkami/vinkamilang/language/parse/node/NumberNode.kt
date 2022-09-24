package com.vinkami.vinkamilang.language.parse.node

import com.vinkami.vinkamilang.language.lex.Token

class NumberNode(val tok: Token): BaseNode(tok.startPos, tok.endPos) {
    override fun toString(): String {
        return "$tok"
    }
}