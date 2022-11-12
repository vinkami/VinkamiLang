package com.vinkami.vinkamilang.language.parse.node

import com.vinkami.vinkamilang.language.lex.Position

class FuncNode(val name: IdenNode, val params: List<ParamNode>, val returnType: IdenNode?, val body: BaseNode, startPos: Position, endPos: Position): BaseNode(startPos, endPos) {
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("(fun $name(")
        sb.append(params.joinToString(", "))
        sb.append(")")
        if (returnType != null) sb.append(": $returnType")
        sb.append(" $body)")
        return sb.toString()
    }
}