package com.vinkami.vinkamilang.language.parse.node

import com.vinkami.vinkamilang.language.lex.Token

class UnaryOpNode(val op: Token, val innerNode: BaseNode): BaseNode(op.startPos, innerNode.endPos) {
    override fun toString(): String {
        return "($op $innerNode)"
    }
}