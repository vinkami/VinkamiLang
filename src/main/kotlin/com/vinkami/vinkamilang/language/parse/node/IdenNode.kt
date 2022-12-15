package com.vinkami.vinkamilang.language.parse.node

import com.vinkami.vinkamilang.language.lex.Position
import com.vinkami.vinkamilang.language.lex.Token

class IdenNode(name: Token, val args: List<BaseNode>, val kwargs: Map<Token, BaseNode>, val withCall: Boolean, endPos: Position): BaseNode(name.startPos, endPos) {
    val name = name.value

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(name)
        if (withCall) sb.append("(args: $args; kwargs: $kwargs)")
        return sb.toString()
    }
}