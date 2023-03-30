package com.vinkami.vinkamilang.language.parse.node

import com.vinkami.vinkamilang.language.lex.Position

class ParamNode(val node: IdenNode, val type: IdenNode?, val default: BaseNode?, endPos: Position): BaseNode(node.startPos, endPos) {
    val name = node.name

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(name)
        if (type != null) sb.append(": $type")
        if (default != null) sb.append(" = $default")
        return sb.toString()
    }
}