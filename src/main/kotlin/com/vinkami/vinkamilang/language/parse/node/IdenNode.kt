package com.vinkami.vinkamilang.language.parse.node

import com.vinkami.vinkamilang.language.lex.Token

class IdenNode(name: Token): BaseNode(name.startPos, name.endPos) {
    val name = name.value

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(name)
        sb.append(callStr)
        return sb.toString()
    }
}