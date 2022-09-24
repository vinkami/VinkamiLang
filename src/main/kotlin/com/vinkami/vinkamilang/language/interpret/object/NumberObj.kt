package com.vinkami.vinkamilang.language.interpret.`object`

class NumberObj(override val value: Float): BaseObject {
    override fun toString(): String {
        return value.toString()
    }
}