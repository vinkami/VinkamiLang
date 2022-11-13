package com.vinkami.vinkamilang.language

import com.vinkami.vinkamilang.language.lex.Token
import com.vinkami.vinkamilang.language.lex.TokenType


@Suppress("MemberVisibilityCanBePrivate")
internal object Constant {
    val arithmeticOp: List<TokenType> = listOf(
        TokenType.PLUS, TokenType.MINUS,
        TokenType.MULTIPLY, TokenType.DIVIDE,
        TokenType.MODULO,
        TokenType.POWER,
    )

    val comparitiveOp: List<TokenType> = listOf(
        TokenType.EQUAL, TokenType.NOT_EQUAL,
        TokenType.LESS, TokenType.LESS_EQUAL,
        TokenType.GREATER, TokenType.GREATER_EQUAL,
    )

    val difinitiveOp: List<TokenType> = listOf(
        TokenType.ASSIGN,
//        TokenType.INCREMENT, TokenType.DECREMENT,
        TokenType.PLUS_ASSIGN, TokenType.MINUS_ASSIGN,
        TokenType.MULTIPLY_ASSIGN, TokenType.DIVIDE_ASSIGN,
        TokenType.MODULO_ASSIGN, TokenType.POWER_ASSIGN,
    )

    val operators: List<TokenType> = arithmeticOp + comparitiveOp + difinitiveOp

    val bracket: Map<TokenType, TokenType> = mapOf(
        TokenType.L_PARAN to TokenType.R_PARAN,
        TokenType.L_BRAC to TokenType.R_BRAC,
        TokenType.L_BRACE to TokenType.R_BRACE,
    )

    val fixValuedTokenPair: Map<String, TokenType> = mapOf(
        // Key char
        "(" to TokenType.L_PARAN,
        ")" to TokenType.R_PARAN,
        "[" to TokenType.L_BRAC,
        "]" to TokenType.R_BRAC,
        "{" to TokenType.L_BRACE,
        "}" to TokenType.R_BRACE,
        "," to TokenType.COMMA,
        "." to TokenType.DOT,
        ":" to TokenType.COLON,
        ";" to TokenType.SEMICOLON,

        // Arithmetic operator
        "+" to TokenType.PLUS,
        "-" to TokenType.MINUS,
        "*" to TokenType.MULTIPLY,
        "/" to TokenType.DIVIDE,
        "**" to TokenType.POWER,
        "%" to TokenType.MODULO,

        // Comparative operator
        "==" to TokenType.EQUAL,  // Combined from 2 ASSIGN
        "!=" to TokenType.NOT_EQUAL,  // Combined from NOT and ASSIGN
        "<" to TokenType.LESS,
        ">" to TokenType.GREATER,
        "<=" to TokenType.LESS_EQUAL,  // Combined from LESS and ASSIGN
        ">=" to TokenType.GREATER_EQUAL,  // Combined from GREATER and ASSIGN

        // Definitive operator
        "=" to TokenType.ASSIGN,
        "+=" to TokenType.PLUS_ASSIGN,  // Combined from PLUS and ASSIGN
        "-=" to TokenType.MINUS_ASSIGN,  // Combined from MINUS and ASSIGN
        "*=" to TokenType.MULTIPLY_ASSIGN,  // Combined from MULTIPLY and ASSIGN
        "/=" to TokenType.DIVIDE_ASSIGN,  // Combined from DIVIDE and ASSIGN
        "%=" to TokenType.MODULO_ASSIGN,  // Combined from MODULO and ASSIGN
        "**=" to TokenType.POWER_ASSIGN,  // Combined from POWER and ASSIGN

        // Logic operator
        "&" to TokenType.AND,
        "|" to TokenType.OR,
        "!" to TokenType.NOT,

        // Keyword
        "if" to TokenType.IF,
        "elif" to TokenType.ELIF,
        "else" to TokenType.ELSE,
        "true" to TokenType.TRUE,
        "false" to TokenType.FALSE,

        "for" to TokenType.FOR,
        "while" to TokenType.WHILE,
        "complete" to TokenType.COMPLETE,
        "incomplete" to TokenType.INCOMPLETE,
        "break" to TokenType.BREAK,

        "return" to TokenType.RETURN,
        "fun" to TokenType.FUNC,
        "class" to TokenType.CLASS,

        "var" to TokenType.VAR,
        "is" to TokenType.IS,
        "in" to TokenType.IN,
        "import" to TokenType.IMPORT,

        "EOF" to TokenType.EOF,
    )

    val bindingPower: Map<TokenType, Pair<Int, Int>> = mapOf(
        // Used in Pratt parser
        TokenType.PLUS to Pair(4, 5), TokenType.MINUS to Pair(4, 5),
        TokenType.MULTIPLY to Pair(6, 7), TokenType.DIVIDE to Pair(6, 7),
        TokenType.MODULO to Pair(8, 9), TokenType.POWER to Pair(10, 11),

        TokenType.EQUAL to Pair(2, 3), TokenType.NOT_EQUAL to Pair(2, 3),
        TokenType.LESS to Pair(2, 3), TokenType.LESS_EQUAL to Pair(2, 3),
        TokenType.GREATER to Pair(2, 3), TokenType.GREATER_EQUAL to Pair(2, 3),

        TokenType.ASSIGN to Pair(100, 1),
        TokenType.PLUS_ASSIGN to Pair(100, 1), TokenType.MINUS_ASSIGN to Pair(100, 1),
        TokenType.MULTIPLY_ASSIGN to Pair(100, 1), TokenType.DIVIDE_ASSIGN to Pair(100, 1),
        TokenType.MODULO_ASSIGN to Pair(100, 1), TokenType.POWER_ASSIGN to Pair(100, 1),
    )

    val conbinableTokens: Map<Pair<TokenType, TokenType>, Pair<TokenType, (Token, Token) -> String>> = mapOf(
//        Pair(TokenType.PLUS, TokenType.PLUS) to Pair(TokenType.INCREMENT) { _, _ -> "++" },
        Pair(TokenType.PLUS, TokenType.ASSIGN) to Pair(TokenType.PLUS_ASSIGN) { _, _ -> "+=" },

//        Pair(TokenType.MINUS, TokenType.MINUS) to Pair(TokenType.DECREMENT) { _, _ -> "--" },
        Pair(TokenType.MINUS, TokenType.ASSIGN) to Pair(TokenType.MINUS_ASSIGN) { _, _ -> "-=" },

        Pair(TokenType.MULTIPLY, TokenType.MULTIPLY) to Pair(TokenType.POWER) { _, _ -> "**" },
        Pair(TokenType.MULTIPLY, TokenType.ASSIGN) to Pair(TokenType.MULTIPLY_ASSIGN) { _, _ -> "*=" },

        Pair(TokenType.POWER, TokenType.ASSIGN) to Pair(TokenType.POWER_ASSIGN) { _, _ -> "**=" },

        Pair(TokenType.MODULO, TokenType.ASSIGN) to Pair(TokenType.MODULO_ASSIGN) { _, _ -> "%=" },

        Pair(TokenType.ASSIGN, TokenType.ASSIGN) to Pair(TokenType.EQUAL) {_, _ -> "==" },
        Pair(TokenType.LESS, TokenType.ASSIGN) to Pair(TokenType.LESS_EQUAL) { _, _ -> "<=" },
        Pair(TokenType.GREATER, TokenType.ASSIGN) to Pair(TokenType.GREATER_EQUAL) { _, _ -> ">=" },
        Pair(TokenType.NOT, TokenType.ASSIGN) to Pair(TokenType.NOT_EQUAL) { _, _ -> "!=" },

        Pair(TokenType.SPACE, TokenType.SPACE) to Pair(TokenType.SPACE) { t1, t2 -> t1.value + t2.value },
        Pair(TokenType.LINEBREAK, TokenType.LINEBREAK) to Pair(TokenType.LINEBREAK) { t1, t2 -> t1.value + t2.value },
    )

    val loopCompleteTT: List<TokenType> = listOf(
        TokenType.COMPLETE, TokenType.INCOMPLETE
    )

    /**
     * Provides `String in Regex` syntax, which can be used in when statements
     *
     * @param other the string to be matched with the regex
     */
    operator fun Regex.contains(other: CharSequence): Boolean = this.matches(other)
}