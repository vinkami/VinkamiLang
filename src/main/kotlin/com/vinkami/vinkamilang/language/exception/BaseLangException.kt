package com.vinkami.vinkamilang.language.exception

import com.vinkami.vinkamilang.language.lex.Position

abstract class BaseLangException(message: String, private val startPos: Position, private val endPos: Position): Exception(message) {
    override fun toString(): String {
        return """
            ${this::class.simpleName}: $message
            File: ${startPos.fileName}, line: ${startPos.line + 1}
            
            ${startPos.code.split('\n')[startPos.line]}
            ${makeArrow(startPos, endPos)}
        """.trimIndent()
    }

    private fun makeArrow(startPos: Position, endPos: Position): String {
        val result = StringBuilder()
        result.append(" ".repeat(startPos.column))
        result.append("^".repeat(endPos.column - startPos.column))
        return result.toString()
    }
}