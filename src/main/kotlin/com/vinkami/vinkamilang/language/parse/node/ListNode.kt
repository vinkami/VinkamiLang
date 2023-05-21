package com.vinkami.vinkamilang.language.parse.node

import com.vinkami.vinkamilang.language.lex.Position

class ListNode(val nodes: List<BaseNode>, startPos: Position, endPos: Position): BaseNode(startPos, endPos) {
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("[")
        for (node in nodes) {
            sb.append(node)
            sb.append(", ")
        }
        sb.append("]")
        sb.append(callStr)
        return sb.toString()
    }
}