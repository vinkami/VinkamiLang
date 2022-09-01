package com.vinkami.vinkamilang.language.`object`

class NumberObj(override val value: Float): BaseObject {
    override fun toString(): String {
        return value.toString()
    }
}