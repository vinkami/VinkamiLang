package com.vinkami.vinkamilang.language

import com.vinkami.vinkamilang.language.exception.ParsingException
import com.vinkami.vinkamilang.language.expression.*
import com.vinkami.vinkamilang.language.expression.Number


class Parser(private val tokens: List<Token>) {
    private var pos = -1
    private val currentToken: Token
        get() = tokens[pos]
    private val nextNonSpaceToken: () -> Token
        get() = {
            if (pos >= tokens.size - 1) {
                tokens.last()
            } else {
                var i = pos + 1
                while (listOf(TokenType.SPACE, TokenType.LINEBREAK).contains(tokens[i].type)) {i++}
                tokens[i]
            }
        }

    init {advance()}

    private fun advance() {
        if (pos  == tokens.size - 1) {
            throw ParsingException("Unexpected end of input", currentToken.position)
        }
        pos++
    }

    private fun skipSpace() {
        while (listOf(TokenType.SPACE, TokenType.LINEBREAK).contains(currentToken.type)) advance()
    }

    fun parse(): Expression {
        skipSpace()
        val expr = when (currentToken.type) {
            TokenType.NUMBER, TokenType.L_PARAN, TokenType.IDENTIFIER -> exprMath(0)
            TokenType.IF -> exprIf()
            TokenType.EOF -> Null()
            else -> throw ParsingException("Unexpected token $currentToken", currentToken.position)
            // TODO("Other expr types not implemented")
        }

        return expr
    }

    private fun exprMath(minBP: Int): Expression {
        if (minBP != 0) {advance()}  // Advance first if it's called by itself
        skipSpace()

        var lhs: Expression = if (currentToken.type == TokenType.L_PARAN) {
            exprBracket()
        } else {
            Number(currentToken)
        }

        while (true) {
            val op = nextNonSpaceToken()
            if (!(Constant.arithmeticOp + Constant.comparitiveOp).contains(op.type)) {break}

            val (leftBP, rightBP) = Constant.bindingPower[op.type]!!
            if (leftBP < minBP) {break}
            advance()
            skipSpace()

            val rhs = exprMath(rightBP)
            lhs = Math(op, lhs, rhs)
        }
        return lhs
    }

    private fun exprBracket(): Expression {
        val bracketTypeL = currentToken.type
        val bracketTypeR = Constant.bracket[bracketTypeL]!!
        var paranCount = 1
        val start = pos
        val startToken = currentToken
        while (paranCount > 0) {
            advance()
            val tt = currentToken.type
            if (tt == TokenType.EOF) {throw ParsingException("Script ended with unclosed bracket", startToken.position)}
            else if (Constant.bracket.keys.contains(tt)) {paranCount++}
            else if (Constant.bracket.values.contains(tt)) {paranCount--}
        }
        if (currentToken.type != bracketTypeR) {
            val ctpos = currentToken.position
            val pos = Position(ctpos.filename, ctpos.lineNumber, ctpos.start, ctpos.end)
            throw ParsingException("Expected closing bracket ${bracketTypeR.name} and found ${currentToken.type}", pos)
        }
        val end = pos
        val endToken = currentToken
        val eofPosition = Position(endToken.position.filename, endToken.position.lineNumber,
                              endToken.position.end + 1, endToken.position.end + 1)
        val innerTokens = tokens.subList(start + 1, end) + Token("EOF", eofPosition)
        val innerExpr = if (innerTokens.isNotEmpty()) Parser(innerTokens).parse() else Null()
        return Bracket(innerExpr, startToken, endToken)
    }

    private fun exprIf(): Expression {
        /**
         * Make sure this.pos points to startToken.position, otherwise the subList will be wrong
         * This function will advance pos to exactly where endTT is at
         *
         * startPos: position of the TT.IF, TT.ELIF for condition; TT.L_BRACE for action
         * endTT: TT.L_BRACE for condition; TT.R_BRACE for action
         */
        fun captureTokensAndParse(endTT: TokenType): Expression {
            val startPos = currentToken.position
            skipSpace()
            val start = pos
            while (currentToken.type != endTT) {
                if (currentToken.type == TokenType.EOF) {
                    throw ParsingException("Script ended with unclosed if statement", startPos)
                }
                advance()
            }

            val eofPosition = Position(currentToken.position.filename, currentToken.position.lineNumber,
                              currentToken.position.end + 1, currentToken.position.end + 1)
            val tokenSection = tokens.subList(start + 1, pos) + Token("EOF", eofPosition)
            return Parser(tokenSection).parse()
        }

        val mainCondition = captureTokensAndParse(TokenType.L_BRACE)  // if ... {
        val mainAction = captureTokensAndParse(TokenType.R_BRACE)  // { ... }

        val elif = mutableMapOf<Expression, Expression>()
        advance()
        skipSpace()
        while (currentToken.type == TokenType.ELIF) {
            val condition = captureTokensAndParse(TokenType.L_BRACE)
            val action = captureTokensAndParse(TokenType.R_BRACE)
            elif[condition] = action
            advance()
            skipSpace()
        }

        var elseAction: Expression = Null()
        if (currentToken.type == TokenType.ELSE) {
            while (currentToken.type != TokenType.L_BRACE) {advance()}
            elseAction = captureTokensAndParse(TokenType.R_BRACE)
            advance()
            skipSpace()
        }

        return If(mainCondition, mainAction, elif, elseAction)
    }
}