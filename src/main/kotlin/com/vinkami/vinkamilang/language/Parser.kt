package com.vinkami.vinkamilang.language

import com.vinkami.vinkamilang.language.exception.ParsingException
import com.vinkami.vinkamilang.language.expression.*
import com.vinkami.vinkamilang.language.position.LexingPosition
import com.vinkami.vinkamilang.language.position.ParsingPosition


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

    private fun aas(){
        advance()
        skipSpace()
    }

    fun parse(): Expression {
        skipSpace()
        val expr = when (currentToken.type) {
            TokenType.NUMBER, TokenType.L_PARAN, TokenType.IDENTIFIER -> exprMath(0)
            TokenType.IF -> exprIf()
            TokenType.WHILE -> exprWhile()
            TokenType.EOF -> Null()
            else -> throw ParsingException("Unexpected token $currentToken", currentToken.position)
            // TODO("Other expr types not implemented")
        }

        return expr
    }

    private fun gotoNext(TT: TokenType){
        while (currentToken.type != TT) {
            if (currentToken.type == TokenType.EOF) {
                throw ParsingException("Script ended when expecting a $TT", currentToken.position)
            }
            advance()
        }
    }

    private fun exprMath(minBP: Int): Expression {
        if (minBP != 0) {advance()}  // Advance first if it's called by itself
        skipSpace()

        var lhs: Expression = if (currentToken.type == TokenType.L_PARAN) {
            exprBracket()
        } else {
            val ctpos = currentToken.position
            val ppos = ParsingPosition(pos, pos, ctpos, ctpos)
            Number(currentToken, ppos)
        }

        while (true) {
            val op = nextNonSpaceToken()
            if (!(Constant.arithmeticOp + Constant.comparitiveOp).contains(op.type)) {break}

            val (leftBP, rightBP) = Constant.bindingPower[op.type]!!
            if (leftBP < minBP) {break}
            aas()

            val rhs = exprMath(rightBP)
            val ppos = ParsingPosition(lhs.position.start, rhs.position.end,
                                       lhs.position.startTokenPosition, rhs.position.endTokenPosition)
            lhs = Math(op, lhs, rhs, ppos)
        }
        return lhs
    }

    private fun exprBracket(): Expression {
        val bracketTypeL = currentToken.type
        val bracketTypeR = Constant.bracket[bracketTypeL]!!
        var paranCount = 1
        val start = pos
        val startToken = currentToken

        while (paranCount > 0) {  // Find the matching closing bracket in terms of number
            advance()
            val tt = currentToken.type
            if (tt == TokenType.EOF) {throw ParsingException("Script ended with unclosed bracket", startToken.position)}
            else if (Constant.bracket.keys.contains(tt)) {paranCount++}
            else if (Constant.bracket.values.contains(tt)) {paranCount--}
        }

        if (currentToken.type != bracketTypeR) {  // Confirm the "matching" bracket is the same type
            val ctpos = currentToken.position
            val pos = LexingPosition(ctpos.filename, ctpos.lineNumber, ctpos.start, ctpos.end)
            throw ParsingException("Expected closing bracket ${bracketTypeR.name} and found ${currentToken.type}", pos)
        }

        val end = pos
        val endToken = currentToken
        val eofPosition = LexingPosition(endToken.position.filename, endToken.position.lineNumber,
                                    endToken.position.end - 1, endToken.position.end - 1)

        val innerTokens = tokens.subList(start + 1, end) + Token("EOF", eofPosition)

        val innerExpr = if (innerTokens.isNotEmpty()) Parser(innerTokens).parse() else Null()

        val ppos = ParsingPosition(start, end, startToken.position, endToken.position)
        return Bracket(innerExpr, startToken, endToken, ppos)
    }

    private fun exprIf(): Expression {
        val start = pos
        val startToken = currentToken

        aas()
        val mainCondition = exprBracket()  // if (...) {
        aas()
        val mainAction = exprBracket()  // { ... }

        val elif = mutableMapOf<Expression, Expression>()
        aas()
        while (currentToken.type == TokenType.ELIF) {
            val condition = exprBracket()  // elif (...) {
            aas()
            val action = exprBracket()  // { ... }
            elif[condition] = action
            aas()
        }

        var elseAction: Expression = Null()
        if (currentToken.type == TokenType.ELSE) {
            gotoNext(TokenType.L_BRACE)
            elseAction = exprBracket()  // { ... }
            aas()
        }

        val end = pos
        val endToken = currentToken

        val ppos = ParsingPosition(start, end, startToken.position, endToken.position)
        return If(mainCondition, mainAction, elif, elseAction, ppos)
    }

    private fun exprWhile(): Expression {
        val start = pos
        val startToken = currentToken

        aas()
        val condition = exprBracket()  // while (...) {
        aas()
        val mainAction = exprBracket()  // { ... }

        aas()
        var completeAction: Expression = Null()
        var incompleteAction: Expression = Null()
        while (listOf(TokenType.COMPLETE, TokenType.INCOMPLETE).contains(currentToken.type)) {
            val tokenType = currentToken.type
            gotoNext(TokenType.L_BRACE)
            val action = exprBracket()  // { ... }
            if (tokenType == TokenType.COMPLETE) {
                if (completeAction !is Null) throw ParsingException("While block following with multiple complete actions", currentToken.position)
                completeAction = action
            } else {
                if (incompleteAction !is Null) throw ParsingException("While block following with multiple incomplete actions", currentToken.position)
                incompleteAction = action
            }

            aas()
        }

        val end = pos
        val endToken = currentToken

        val ppos = ParsingPosition(start, end, startToken.position, endToken.position)
        return While(condition, mainAction, completeAction, incompleteAction, ppos)
    }

    private fun exprFor(): Expression {
        TODO("Not yet implemented")
    }


}