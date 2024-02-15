package com.vinkami.vinkamilang.language.lex

import com.vinkami.vinkamilang.language.Constant
import com.vinkami.vinkamilang.language.Constant.contains
import com.vinkami.vinkamilang.language.exception.IllegalCharError
import com.vinkami.vinkamilang.language.lex.TokenType.*


class Token {
    var type: TokenType
    var value: String
    var startPos: Position
    var endPos: Position

    constructor(section: String, startPos: Position, endPos: Position) {
        this.startPos = startPos
        this.endPos = endPos

        val pair = determineTokenPair(section)
        this.type = pair.first
        this.value = pair.second
    }

    constructor(TT: TokenType, value: String, startPos: Position, endPos: Position) {
        this.type = TT
        this.value = value
        this.startPos = startPos
        this.endPos = endPos
    }

    override fun toString(): String {
        return if (type in listOf(NUMBER, STRING, IDENTIFIER)) {
            "<$type: $value>"
        } else {
            "<$type>"
        }
    }

    private fun determineTokenPair(section: String): Pair<TokenType, String> {
        Constant.fixValuedTokenPair[section]?.let { return it to section }

        return when (section) {
            in Regex("^[0-9.]+$") -> NUMBER to section
            in Regex("^[a-zA-Z_][a-zA-Z0-9_]*$") -> IDENTIFIER to section
            in Regex("^(?:\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\"|'[^'\\\\]*(?:\\\\.[^'\\\\]*)*')\$") -> STRING to section.substring(1, section.length - 1)
            in Regex("^ +$") -> SPACE to section
            in Regex("^[\r\n]+$") -> LINEBREAK to section
            in Regex("^import [^\n]*", RegexOption.IGNORE_CASE) -> IMPORT to section.substring(7, section.length - 1)
            else -> throw IllegalCharError(section, startPos, endPos)
        }
    }
}


