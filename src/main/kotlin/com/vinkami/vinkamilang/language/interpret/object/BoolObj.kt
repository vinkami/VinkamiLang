package com.vinkami.vinkamilang.language.interpret.`object`

class BoolObj(override val value: Boolean): BaseObject {
    override fun toString(): String {
        return if (value) "true" else "false"
    }
}