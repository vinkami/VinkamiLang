package com.vinkami.vinkamilang.language

class LexingException(message: String, private val position: Position): Exception(message) {
    override fun toString(): String {
        return """
            $position
            LexingException: $message
        """.trimIndent()
    }
}

class ParsingException(message: String, private val position: Position): Exception(message) {
    override fun toString(): String {
        return """
            $position
            ParsingException: $message
        """.trimIndent()
    }
}

class DebugMessage(message: String, private val position: Position): Exception(message) {
    override fun toString(): String {
        return """
            $position
            DebugMessage: $message
        """.trimIndent()
    }
}