package com.vinkami.vinkamilang.language.exception

import com.vinkami.vinkamilang.language.position.LexingPosition

class InterpretingException(message: String, position: LexingPosition, val errorName: String): BaseLangException(message, position) {
    override fun toString(): String {
        return """
            $position
            $errorName: $message
        """.trimIndent()
    }
}
