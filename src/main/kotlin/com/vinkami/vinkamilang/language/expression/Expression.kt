package com.vinkami.vinkamilang.language.expression

abstract class Expression(val value: Any) {
    override fun toString(): String {
        return this.value.toString()
    }
}