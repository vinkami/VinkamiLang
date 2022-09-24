package com.vinkami.vinkamilang.language.interpret.`object`

class NullObj: BaseObject {
    override val value = "null"
    override fun toString(): String {
        return "null"
    }
}