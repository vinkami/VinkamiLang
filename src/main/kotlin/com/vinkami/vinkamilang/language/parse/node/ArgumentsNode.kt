package com.vinkami.vinkamilang.language.parse.node

import com.vinkami.vinkamilang.language.lex.Position
import com.vinkami.vinkamilang.language.lex.Token

class ArgumentsNode(val args: List<BaseNode>, val kwargs: Map<Token, BaseNode>, startPos: Position, endPos: Position): BaseNode(startPos, endPos) {
    override fun toString(): String {
        return "(args: $args, kwargs: $kwargs)$callStr"
    }
}