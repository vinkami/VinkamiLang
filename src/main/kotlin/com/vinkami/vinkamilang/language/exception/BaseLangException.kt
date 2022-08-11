package com.vinkami.vinkamilang.language.exception

import com.vinkami.vinkamilang.language.position.LexingPosition

abstract class BaseLangException(message: String, val position: LexingPosition): Exception(message) {
    override fun toString(): String {
        return """
            $position
            LexingException: $message
        """.trimIndent()
    }
}