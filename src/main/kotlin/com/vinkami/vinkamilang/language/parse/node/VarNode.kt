package com.vinkami.vinkamilang.language.parse.node

import com.vinkami.vinkamilang.language.lex.Token

class VarNode(val token: Token): BaseNode(token.startPos, token.endPos) {
    val name = token.value
    override fun toString(): String {
        return "\$$name"
    }
}
