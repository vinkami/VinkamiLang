package com.vinkami.vinkamilang.language


object Constant {
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
        TokenType.INCREMENT, TokenType.DECREMENT,
        TokenType.PLUS_ASSIGN, TokenType.MINUS_ASSIGN,
        TokenType.MULTIPLY_ASSIGN, TokenType.DIVIDE_ASSIGN,
        TokenType.MODULO_ASSIGN, TokenType.POWER_ASSIGN,
    )

    val operator: List<TokenType> = arithmeticOp + comparitiveOp + difinitiveOp

    val bracket: Map<TokenType, TokenType> = mapOf(
        TokenType.L_PARAN to TokenType.R_PARAN,
        TokenType.L_BRAC to TokenType.R_BRAC,
        TokenType.L_BRACE to TokenType.R_BRACE,
    )

    val bindingPower: Map<TokenType, Pair<Int, Int>> = mapOf(
        TokenType.PLUS to Pair(4, 5), TokenType.MINUS to Pair(4, 5),
        TokenType.MULTIPLY to Pair(6, 7), TokenType.DIVIDE to Pair(6, 7),
        TokenType.MODULO to Pair(8, 9),
        TokenType.POWER to Pair(10, 11),

        TokenType.EQUAL to Pair(2, 3), TokenType.NOT_EQUAL to Pair(2, 3),
        TokenType.LESS to Pair(2, 3), TokenType.LESS_EQUAL to Pair(2, 3),
        TokenType.GREATER to Pair(2, 3), TokenType.GREATER_EQUAL to Pair(2, 3),
    )
}