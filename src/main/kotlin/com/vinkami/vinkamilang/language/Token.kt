package com.vinkami.vinkamilang.language

class Token(section: String) {
    private val pair = determineTokenPair(section)

    val type = this.pair.first
    val value = this.pair.second

    override fun toString(): String {
        return if (this.value == null){"<${this.type}>"} else {"<${this.type}: ${this.value}>"}
    }
}

fun determineTokenPair(section: String): Pair<TokenType, Any?> {
    return when (section) {
        // Key char
        "(" -> Pair(TokenType.L_PARAN, null)
        ")" -> Pair(TokenType.R_PARAN, null)
        "[" -> Pair(TokenType.L_BRAC, null)
        "]" -> Pair(TokenType.R_BRAC, null)
        "," -> Pair(TokenType.COMMA, null)
        "." -> Pair(TokenType.DOT, null)
        ":" -> Pair(TokenType.COLON, null)

        // Operator
        "+" -> Pair(TokenType.PLUS, null)
        "-" -> Pair(TokenType.MINUS, null)
        "*" -> Pair(TokenType.MULTIPLY, null)
        "/" -> Pair(TokenType.DIVIDE, null)
        "^" -> Pair(TokenType.POWER, null)
        "%" -> Pair(TokenType.MODULO, null)

        // Comparator
        "==" -> Pair(TokenType.EQUAL, null)
        "!=" -> Pair(TokenType.NOT_EQUAL, null)
        "<" -> Pair(TokenType.LESS, null)
        ">" -> Pair(TokenType.GREATER, null)
        "<=" -> Pair(TokenType.LESS_EQUAL, null)
        ">=" -> Pair(TokenType.GREATER_EQUAL, null)

        // Logic
        "&&" -> Pair(TokenType.AND, null)
        "||" -> Pair(TokenType.OR, null)
        "!" -> Pair(TokenType.NOT, null)

        // Keyword
        "if" -> Pair(TokenType.IF, null)
        "elif" -> Pair(TokenType.ELIF, null)
        "else" -> Pair(TokenType.ELSE, null)
        "true" -> Pair(TokenType.TRUE, null)
        "false" -> Pair(TokenType.FALSE, null)
        "for" -> Pair(TokenType.FOR, null)
        "while" -> Pair(TokenType.WHILE, null)
        "return" -> Pair(TokenType.RETURN, null)
        "var" -> Pair(TokenType.VAR, null)
        "is" -> Pair(TokenType.IS, null)
        "import" -> Pair(TokenType.IMPORT, null)
        "in" -> Pair(TokenType.IN, null)

        // Format
        " " -> Pair(TokenType.SPACE, null)
        "\n" -> Pair(TokenType.LINEBREAK, null)
        "EOF" -> Pair(TokenType.EOF, null)

        else -> {
            if (Regex("^[0-9.]+$").matches(section)) {
                Pair(TokenType.NUMBER, section)
            } else if (Regex("^[a-zA-Z_][a-zA-Z0-9_]*$").matches(section)) {
                Pair(TokenType.IDENTIFIER, section)
            } else if (Regex("^\"[^\"]*\"$").matches(section) || Regex("^'[^']*'$").matches(section)) {
                Pair(TokenType.STRING, section.substring(1, section.length - 1))
            } else {
                Pair(TokenType.UNKNOWN, section)
            }
        }
    }
}
