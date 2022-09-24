package com.vinkami.vinkamilang.language.parse.node

import com.vinkami.vinkamilang.language.lex.Token

class UnaryOpNode(val op: Token, val node: BaseNode): BaseNode(op.startPos, node.endPos) {
    override fun toString(): String {
        return "($op $node)"
    }
}