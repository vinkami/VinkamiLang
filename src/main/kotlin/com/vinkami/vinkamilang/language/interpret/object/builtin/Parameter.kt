package com.vinkami.vinkamilang.language.interpret.`object`.builtin

import com.vinkami.vinkamilang.language.parse.node.BaseNode

class Parameter(val name: String, val type: String?, val default: BaseNode?) {
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(name)
        if (type != null) sb.append(": $type")
        if (default != null) sb.append(" = $default")
        return sb.toString()
    }
}