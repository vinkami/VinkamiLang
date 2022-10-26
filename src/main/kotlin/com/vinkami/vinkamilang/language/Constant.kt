package com.vinkami.vinkamilang.language

import com.vinkami.vinkamilang.language.lex.Token
import com.vinkami.vinkamilang.language.lex.TokenType


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

    val bindingPower: Map<TokenType, Pair<Int, Int>> = mapOf(
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
}