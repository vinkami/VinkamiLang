package com.vinkami.vinkamilang.language

import com.vinkami.vinkamilang.language.expression.*
import com.vinkami.vinkamilang.language.expression.Number


class Parser(private val tokens: List<Token>) {
    private var pos = -1
    private val currentToken: Token?
        get() = if (pos < tokens.size) tokens[pos] else null
    private val nextToken: Token?
        get() = if (pos + 1 < tokens.size) tokens[pos + 1] else null
    private val nextNonSpaceToken: () -> Token?
        get() = {
            var i = pos + 1
            while (i < tokens.size && tokens[i].type == TokenType.SPACE) {i++}
            if (i < tokens.size) tokens[i] else null
        }

    init {advance()}

    private fun advance() {pos++}

    private fun skipSpace() {
        while (currentToken?.type == TokenType.SPACE) advance()
    }

    fun parse(): Expression {
        val expr = when (currentToken!!.type) {
            TokenType.NUMBER, TokenType.L_PARAN, TokenType.IDENTIFIER -> exprArithmetic(0)
            TokenType.SPACE -> {
                advance()
                parse()
            }
            TokenType.IF -> exprIf()
            else -> throw ParsingException("Unexpected token ${currentToken!!}", currentToken!!.position)
            // TODO("Other expr types not implemented")
        }

        return expr
    }

    private fun exprArithmetic(minBP: Int): Expression {
        if (minBP != 0) {advance()}  // Advance first if it's called by itself
        skipSpace()

        var lhs: Expression = if (currentToken!!.type == TokenType.L_PARAN) {
            exprBracket()
        } else {
            Number(currentToken!!)
        }

        while (true) {
            val op = nextNonSpaceToken()
            if (!Constant.arithmeticOp.contains(op?.type)) {break}

            val (leftBP, rightBP) = bindingPower(op!!.type)
            if (leftBP < minBP) {break}
            advance()
            skipSpace()

            val rhs = exprArithmetic(rightBP)
            lhs = Arithmetic(op, lhs, rhs)
        }
        return lhs
    }

    private fun bindingPower(tt: TokenType): Pair<Int, Int> {
        return when (tt) {
            TokenType.PLUS -> Pair(1, 2)
            TokenType.MINUS -> Pair(1, 2)
            TokenType.MULTIPLY -> Pair(3, 4)
            TokenType.DIVIDE -> Pair(3, 4)
            TokenType.MODULO -> Pair(5, 6)
            TokenType.POWER -> Pair(7, 8)
            else -> Pair(0, 0)
        }
    }

    private fun exprBracket(): Expression {
        val bracketTypeL = currentToken!!.type
        val bracketTypeR = Constant.bracket[bracketTypeL]!!
        var paranCount = 1
        val start = pos
        val startToken = currentToken!!
        while (paranCount > 0) {
            advance()
            val tt = currentToken?.type
            if (tt == TokenType.EOF) {throw ParsingException("Script ended with unclosed bracket", startToken.position)}
            else if (Constant.bracket.keys.contains(tt)) {paranCount++}
            else if (Constant.bracket.values.contains(tt)) {paranCount--}
        }
        if (currentToken!!.type != bracketTypeR) {
            val ctpos = currentToken!!.position
            val pos = Position(ctpos.filename, ctpos.lineNumber, ctpos.start, ctpos.end)
            throw ParsingException("Expected closing bracket ${bracketTypeR.name} and found ${currentToken?.type}", pos)
        }
        val end = pos
        val endToken = currentToken!!
        val innerTokens = tokens.subList(start + 1, end)
        val innerExpr = if (innerTokens.isNotEmpty()) Parser(innerTokens).parse() else Null()
        return Bracket(innerExpr, startToken, endToken)
    }


    private fun exprIf(): Expression {
//        val start = pos
//        while (currentToken?.type != TokenType.COLON) {advance()}
//        val colon = pos
        throw ParsingException("Not implemented", currentToken!!.position)
        // TODO()
    }
}