package com.vinkami.vinkamilang.language.parse.node

import com.vinkami.vinkamilang.language.lex.Token

class BracketNode(val left: Token, val node: BaseNode, val right: Token): BaseNode(left.startPos, right.endPos) {
    override fun toString(): String {
        return "($left $node $right)"
    }
}