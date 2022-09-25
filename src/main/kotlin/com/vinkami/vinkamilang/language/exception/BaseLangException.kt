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
        val spaces = startPos.column
        for (i in 0 until spaces) {
            result.append(" ")
        }

        val errorLen = endPos.column - startPos.column
        for (i in 0 until errorLen) {
            result.append("^")
        }

        return result.toString()
    }
}