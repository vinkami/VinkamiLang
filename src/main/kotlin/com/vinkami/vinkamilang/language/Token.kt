package com.vinkami.vinkamilang.language

class Token{
    var type: TokenType
    var value: String?
    var position: Position

    constructor(section: String, position: Position) {
        val pair = determineTokenPair(section)

        this.type = pair.first
        this.value = pair.second
        this.position = position
    }

    constructor(TT: TokenType, value: String?, position: Position) {
        this.type = TT
        this.value = value
        this.position = position
    }

    override fun toString(): String {
        return if (value == null){"<${type}>"} else {"<${type}: ${value}>"}
    }

    private fun determineTokenPair(section: String): Pair<TokenType, String?> {
        return when (section) {
            // Key char
            "(" -> Pair(TokenType.L_PARAN, null)
            ")" -> Pair(TokenType.R_PARAN, null)
            "[" -> Pair(TokenType.L_BRAC, null)
            "]" -> Pair(TokenType.R_BRAC, null)
            "{" -> Pair(TokenType.L_BRACE, null)
            "}" -> Pair(TokenType.R_BRACE, null)
            "," -> Pair(TokenType.COMMA, null)
            "." -> Pair(TokenType.DOT, null)
            ":" -> Pair(TokenType.COLON, null)

            // Arithmetic operator
            "+" -> Pair(TokenType.PLUS, null)
            "-" -> Pair(TokenType.MINUS, null)
            "*" -> Pair(TokenType.MULTIPLY, null)
            "/" -> Pair(TokenType.DIVIDE, null)
            "**" -> Pair(TokenType.POWER, null)
            "%" -> Pair(TokenType.MODULO, null)

            // Comparative operator
            "==" -> Pair(TokenType.EQUAL, null)  // Combined from 2 ASSIGN
            "!=" -> Pair(TokenType.NOT_EQUAL, null)  // Combined from NOT and ASSIGN
            "<" -> Pair(TokenType.LESS, null)
            ">" -> Pair(TokenType.GREATER, null)
            "<=" -> Pair(TokenType.LESS_EQUAL, null)  // Combined from LESS and ASSIGN
            ">=" -> Pair(TokenType.GREATER_EQUAL, null)  // Combined from GREATER and ASSIGN

            // Definitive operator
            "=" -> Pair(TokenType.ASSIGN, null)
            "++" -> Pair(TokenType.INCREMENT, null)  // Combined from 2 PLUS
            "--" -> Pair(TokenType.DECREMENT, null)  // Combined from 2 MINUS
            "+=" -> Pair(TokenType.PLUS_ASSIGN, null)  // Combined from PLUS and ASSIGN
            "-=" -> Pair(TokenType.MINUS_ASSIGN, null)  // Combined from MINUS and ASSIGN
            "*=" -> Pair(TokenType.MULTIPLY_ASSIGN, null)  // Combined from MULTIPLY and ASSIGN
            "/=" -> Pair(TokenType.DIVIDE_ASSIGN, null)  // Combined from DIVIDE and ASSIGN
            "%=" -> Pair(TokenType.MODULO_ASSIGN, null)  // Combined from MODULO and ASSIGN
            "**=" -> Pair(TokenType.POWER_ASSIGN, null)  // Combined from POWER and ASSIGN

            // Logic
            "&" -> Pair(TokenType.AND, null)
            "|" -> Pair(TokenType.OR, null)
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

            "EOF" -> Pair(TokenType.EOF, null)

            else -> {
                if (Regex("^[0-9.]+$").matches(section)) {
                    Pair(TokenType.NUMBER, section)
                } else if (Regex("^[a-zA-Z_][a-zA-Z0-9_]*$").matches(section)) {
                    Pair(TokenType.IDENTIFIER, section)
                } else if (Regex("^\"([^\\\\\"]|\\\\.)*\"\$").matches(section) || Regex("^'([^\\\\']|\\\\.)*'\$").matches(section)) {
                    Pair(TokenType.STRING, section.substring(1, section.length - 1))
                } else if (Regex("^ +$").matches(section)) {
                    Pair(TokenType.SPACE, section)
                } else if (Regex("^\n+$").matches(section)) {
                    Pair(TokenType.LINEBREAK, section)
                } else {
                    Pair(TokenType.UNKNOWN, section)
                }
            }
        }
    }
}


