package com.vinkami.vinkamilang.language.exception

import com.vinkami.vinkamilang.language.Position

abstract class BaseLangException(message: String, val position: Position): Exception(message) {
    override fun toString(): String {
        return """
            $position
            LexingException: $message
        """.trimIndent()
    }
}