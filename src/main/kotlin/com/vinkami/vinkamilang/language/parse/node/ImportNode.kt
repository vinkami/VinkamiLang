package com.vinkami.vinkamilang.language.parse.node

import com.vinkami.vinkamilang.language.lex.Position

class ImportNode(val lib: String, startPos: Position, endPos: Position): BaseNode(startPos, endPos) {
    override fun toString(): String {
        return "import $lib"
    }
}