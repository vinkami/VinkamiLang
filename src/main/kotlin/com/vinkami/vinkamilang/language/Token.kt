package com.vinkami.vinkamilang.language

class Token(section: String) {
    private val pair = when (section) {
        // Key char
        "(" -> Pair(TokenType.L_PARAN, null)
        ")" -> Pair(TokenType.R_PARAN, null)
        "[" -> Pair(TokenType.L_BRAC, null)
        "]" -> Pair(TokenType.R_BRAC, null)
        "," -> Pair(TokenType.COMMA, null)
        "." -> Pair(TokenType.DOT, null)
        ":" -> Pair(TokenType.COLON, null)
        "!" -> Pair(TokenType.EXCLAM, null)

        // Operator
        "+" -> Pair(TokenType.PLUS, null)
        "-" -> Pair(TokenType.MINUS, null)
        "*" -> Pair(TokenType.MULTIPLY, null)
        "/" -> Pair(TokenType.DIVIDE, null)
        "^" -> Pair(TokenType.POWER, null)
        "%" -> Pair(TokenType.MODULO, null)

        // TODO("Many other TT not yet implemented")

        " " -> Pair(TokenType.SPACE, null)
        "\n" -> Pair(TokenType.LINEBREAK, null)

        else -> Pair(TokenType.IDENTIFIER, section)
    }

    val type = this.pair.first
    val value = this.pair.second

    override fun toString(): String {
        return "<${this.type}: ${this.value}>"
    }

}