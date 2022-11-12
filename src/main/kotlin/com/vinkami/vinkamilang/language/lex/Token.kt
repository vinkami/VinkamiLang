package com.vinkami.vinkamilang.language.lex

import com.vinkami.vinkamilang.language.Constant

class Token {
    var type: TokenType
    var value: String
    var startPos: Position
    var endPos: Position

    constructor(section: String, startPos: Position, endPos: Position) {
        val pair = determineTokenPair(section)

        this.type = pair.first
        this.value = pair.second
        this.startPos = startPos
        this.endPos = endPos
    }

    constructor(TT: TokenType, value: String, startPos: Position, endPos: Position) {
        this.type = TT
        this.value = value
        this.startPos = startPos
        this.endPos = endPos
    }

    override fun toString(): String {
        return if (type in listOf(TokenType.NUMBER, TokenType.STRING, TokenType.IDENTIFIER)) {
            "<$type: $value>"
        } else {
            "<$type>"
        }
    }

    private fun determineTokenPair(section: String): Pair<TokenType, String> {
        Constant.fixValuedTokenPair[section]?.let { return it to section }

        operator fun Regex.contains(other: CharSequence): Boolean = this.matches(other)  // provides `CharSequence in Regex` syntax, which can be used in when statement below
        return when (section) {
            in Regex("^[0-9.]+$") -> TokenType.NUMBER to section
            in Regex("^[a-zA-Z_][a-zA-Z0-9_]*$") -> TokenType.IDENTIFIER to section
            in Regex("^(?:\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\"|'[^'\\\\]*(?:\\\\.[^'\\\\]*)*')\$") -> TokenType.STRING to section.substring(1, section.length - 1)
            in Regex("^ +$") -> TokenType.SPACE to section
            in Regex("^[\r\n]+$") -> TokenType.LINEBREAK to section
            else -> TokenType.UNKNOWN to section
        }
    }
}


