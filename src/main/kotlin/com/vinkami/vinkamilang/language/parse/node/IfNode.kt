package com.vinkami.vinkamilang.language.parse.node

import com.vinkami.vinkamilang.language.lex.Position

class IfNode(val condition: BaseNode, val action: BaseNode, val elif: MutableMap<BaseNode, BaseNode>, val elseAction: BaseNode?, startPos: Position, endPos: Position):
    BaseNode(startPos, endPos) {

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("(if $condition -> $action")

        for ((elifCondition, elifAction) in elif) {
            sb.append(" | $elifCondition -> $elifAction")
        }

        if (elseAction != null) {
            sb.append(" | $elseAction")
        }

        sb.append(")")
        return sb.toString()
    }
}