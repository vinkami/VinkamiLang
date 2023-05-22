package com.vinkami.vinkamilang.language.interpret.`object`.function

import com.vinkami.vinkamilang.language.interpret.`object`.BaseObject

class Parameter(val name: String, val type: String?, val default: BaseObject?, val variable: Boolean = false, val kwvariable: Boolean = false) {
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(name)
        if (type != null) sb.append(": $type")
        if (default != null) sb.append(" = $default")
        return sb.toString()
    }
}