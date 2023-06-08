package com.vinkami.vinkamilang.language.parse.node

import com.vinkami.vinkamilang.language.lex.Position
import com.vinkami.vinkamilang.language.lex.TokenType

class InterruptNode(val innerNode: BaseNode, val type: TokenType, startPos: Position): BaseNode(startPos, innerNode.endPos) {
    override fun toString(): String {
        return "($type $innerNode)"
    }
}