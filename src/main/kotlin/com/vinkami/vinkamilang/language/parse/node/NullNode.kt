package com.vinkami.vinkamilang.language.parse.node

import com.vinkami.vinkamilang.language.lex.Position
import com.vinkami.vinkamilang.language.lex.Token

class NullNode: BaseNode {
    constructor(token: Token): super(token.startPos, token.endPos)
    constructor(startPos: Position, endPos: Position): super(startPos, endPos)

    override fun toString(): String {
        return "null"
    }
}