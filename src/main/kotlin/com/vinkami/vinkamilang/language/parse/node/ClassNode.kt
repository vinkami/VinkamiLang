package com.vinkami.vinkamilang.language.parse.node

import com.vinkami.vinkamilang.language.lex.Position
import com.vinkami.vinkamilang.language.lex.Token

class ClassNode(val name: Token, val initParams: List<ParamNode>, val parent: IdenNode?, val body: BaseNode, startPos: Position, endPos: Position): BaseNode(startPos, endPos) {
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("(class $name")
        if (initParams.isNotEmpty()) {
            sb.append("(")
            sb.append(initParams.joinToString(", "))
            sb.append(")")
        }
        if (parent != null) sb.append(": $parent")
        sb.append(" $body)")
        return sb.toString()
    }
}