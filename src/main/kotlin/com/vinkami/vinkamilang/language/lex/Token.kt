package com.vinkami.vinkamilang.language.lex

class Token {
    var type: TokenType
    var value: String
    var startPos: Position
    var endPos: Position

    constructor(section: String, startPos: Position, endPos: Position){
        val pair = determineTokenPair(section)

        this.type = pair.first
        this.value = pair.second
        this.startPos = startPos
        this.endPos = endPos
    }

    constructor(TT: TokenType, value: String, startPos: Position, endPos: Position){
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
        return when (section) {
            // Key char
            "(" -> Pair(TokenType.L_PARAN, "(")
            ")" -> Pair(TokenType.R_PARAN, ")")
            "[" -> Pair(TokenType.L_BRAC, "[")
            "]" -> Pair(TokenType.R_BRAC, "]")
            "{" -> Pair(TokenType.L_BRACE, "{")
            "}" -> Pair(TokenType.R_BRACE, "}")
            "," -> Pair(TokenType.COMMA, ",")
            "." -> Pair(TokenType.DOT, ".")
            ":" -> Pair(TokenType.COLON, ":")

            // Arithmetic operator
            "+" -> Pair(TokenType.PLUS, "+")
            "-" -> Pair(TokenType.MINUS, "-")
            "*" -> Pair(TokenType.MULTIPLY, "*")
            "/" -> Pair(TokenType.DIVIDE, "/")
            "**" -> Pair(TokenType.POWER, "**")
            "%" -> Pair(TokenType.MODULO, "%")

            // Comparative operator
            "==" -> Pair(TokenType.EQUAL, "==")  // Combined from 2 ASSIGN
            "!=" -> Pair(TokenType.NOT_EQUAL, "!=")  // Combined from NOT and ASSIGN
            "<" -> Pair(TokenType.LESS, "<")
            ">" -> Pair(TokenType.GREATER, ">")
            "<=" -> Pair(TokenType.LESS_EQUAL, "<=")  // Combined from LESS and ASSIGN
            ">=" -> Pair(TokenType.GREATER_EQUAL, ">=")  // Combined from GREATER and ASSIGN

            // Definitive operator
            "=" -> Pair(TokenType.ASSIGN, "=")
            "++" -> Pair(TokenType.INCREMENT, "++")  // Combined from 2 PLUS
            "--" -> Pair(TokenType.DECREMENT, "--")  // Combined from 2 MINUS
            "+=" -> Pair(TokenType.PLUS_ASSIGN, "+=")  // Combined from PLUS and ASSIGN
            "-=" -> Pair(TokenType.MINUS_ASSIGN, "-=")  // Combined from MINUS and ASSIGN
            "*=" -> Pair(TokenType.MULTIPLY_ASSIGN, "*=")  // Combined from MULTIPLY and ASSIGN
            "/=" -> Pair(TokenType.DIVIDE_ASSIGN, "/=")  // Combined from DIVIDE and ASSIGN
            "%=" -> Pair(TokenType.MODULO_ASSIGN, "%=")  // Combined from MODULO and ASSIGN
            "**=" -> Pair(TokenType.POWER_ASSIGN, "**=")  // Combined from POWER and ASSIGN

            // Logic
            "&" -> Pair(TokenType.AND, "&")
            "|" -> Pair(TokenType.OR, "|")
            "!" -> Pair(TokenType.NOT, "!")

            // Keyword
            "if" -> Pair(TokenType.IF, "if")
            "elif" -> Pair(TokenType.ELIF, "elif")
            "else" -> Pair(TokenType.ELSE, "else")
            "true" -> Pair(TokenType.TRUE, "true")
            "false" -> Pair(TokenType.FALSE, "false")
            "for" -> Pair(TokenType.FOR, "for")
            "while" -> Pair(TokenType.WHILE, "while")
            "return" -> Pair(TokenType.RETURN, "return")
            "var" -> Pair(TokenType.VAR, "var")
            "is" -> Pair(TokenType.IS, "is")
            "import" -> Pair(TokenType.IMPORT, "import")
            "in" -> Pair(TokenType.IN, "in")
            "complete" -> Pair(TokenType.COMPLETE, "complete")
            "incomplete" -> Pair(TokenType.INCOMPLETE, "incomplete")
            "break" -> Pair(TokenType.BREAK, "break")

            "EOF" -> Pair(TokenType.EOF, "EOF")

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


