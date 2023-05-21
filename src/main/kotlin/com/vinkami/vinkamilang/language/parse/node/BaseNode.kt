package com.vinkami.vinkamilang.language.parse.node

import com.vinkami.vinkamilang.language.lex.Position

abstract class BaseNode(val startPos: Position, val endPos: Position) {
    var call: ArgumentsNode? = null
    val callStr: String
        get() = if (call == null) "" else call.toString()
}