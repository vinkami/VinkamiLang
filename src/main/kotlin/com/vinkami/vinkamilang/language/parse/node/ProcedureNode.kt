package com.vinkami.vinkamilang.language.parse.node

import com.vinkami.vinkamilang.language.lex.Position

class ProcedureNode(val procedures: List<BaseNode>, startPos: Position, endPos: Position): BaseNode(startPos, endPos) {
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("(")
        for (procedure in procedures) {
            sb.append("$procedure; ")
        }
        sb.append(")")
        sb.append(callStr)
        return sb.toString()
    }
}