package com.vinkami.vinkamilang.language.parse

import com.vinkami.vinkamilang.language.Constant
import com.vinkami.vinkamilang.language.exception.BaseLangException
import com.vinkami.vinkamilang.language.exception.IllegalCharError
import com.vinkami.vinkamilang.language.exception.SyntaxError
import com.vinkami.vinkamilang.language.lex.Token
import com.vinkami.vinkamilang.language.lex.TokenType
import com.vinkami.vinkamilang.language.parse.node.*


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

    private fun advance(): BaseLangException? {
        if (pos  == tokens.size - 1) {
            return SyntaxError("Unexpected end of file", currentToken.startPos, currentToken.endPos)
        }
        pos++
        return null
    }

    private fun skipSpace(): BaseLangException? {
        while (listOf(TokenType.SPACE, TokenType.LINEBREAK).contains(currentToken.type)) {
            advance().let {if (it != null) return it}
        }
        return null
    }

    private fun ass(): BaseLangException? {
        advance().let { if (it != null) return it }
        skipSpace().let { if (it != null) return it }
        return null
    }

    fun parse(): ParseResult {
        skipSpace().let { if (it != null) return ParseResult(it) }

        return when (currentToken.type) {
            TokenType.NUMBER, TokenType.L_PARAN, TokenType.IDENTIFIER -> parseMath(0)
//            TokenType.IF -> stmIf()
//            TokenType.WHILE, TokenType.FOR -> stmLoop()
            TokenType.EOF -> ParseResult(NullNode(currentToken))
            TokenType.UNKNOWN -> ParseResult(IllegalCharError(currentToken))
            else -> ParseResult(SyntaxError("Unexpected token ${currentToken.type}", currentToken.startPos, currentToken.endPos))
            // TODO("Other expr types not implemented")
        }
    }

//    private fun gotoNext(TT: TokenType): BaseLangException? {
//        while (currentToken.type != TT) {
//            if (currentToken.type == TokenType.EOF) {
//                return SyntaxError("Script ended when expecting a $TT", currentToken.startPos, currentToken.endPos)
//            }
//            advance().let { if (it != null) return it }
//        }
//        return null
//    }

    private fun parseMath(minBP: Int): ParseResult {
        val res = ParseResult()
        if (minBP != 0) res(advance())
        res(skipSpace())

        var lhs: BaseNode = if (currentToken.type == TokenType.L_PARAN) {
            val brac = parseBracket()
            brac.node
        } else {
            NumberNode(currentToken)
        }

        while (true) {
            val op = nextNonSpaceToken()
            if (!(Constant.arithmeticOp + Constant.comparitiveOp).contains(op.type)) break

            val (leftBP, rightBP) = Constant.bindingPower[op.type]!!
            if (leftBP < minBP) {break}
            res(ass())

            val rhs = parseMath(rightBP)
            lhs = BinOpNode(lhs, op, rhs.node)
        }
        return res(lhs)
    }


    private fun parseBracket(): ParseResult {
        val res = ParseResult()

        val bracketTypeL = currentToken.type
        val bracketTypeR = Constant.bracket[bracketTypeL]!!
        var paranCount = 1
        val start = pos
        val startToken = currentToken

        while (paranCount > 0) {  // Find the matching closing bracket in terms of number
            res(advance())
            val tt = currentToken.type
            if (tt == TokenType.EOF) throw SyntaxError("Script ended when expecting a $bracketTypeR", currentToken.startPos, currentToken.endPos)
            else if (Constant.bracket.keys.contains(tt)) paranCount++
            else if (Constant.bracket.values.contains(tt)) paranCount--
        }

        // Confirm the "matching" bracket is the same type
        if (currentToken.type != bracketTypeR) res(SyntaxError("Expected $bracketTypeR, got ${currentToken.type}", currentToken.startPos, currentToken.endPos))

        val endToken = currentToken
        val eof = Token(TokenType.EOF, "EOF", endToken.startPos, endToken.endPos)
        val innerTokens = tokens.subList(start + 1, pos) + eof
        val innerResult = res(if (innerTokens.isNotEmpty()) Parser(innerTokens).parse() else ParseResult(NullNode(currentToken)))

        return res(BracketNode(startToken, innerResult.node, endToken))
    }


//    private fun exprMath(minBP: Int): BaseExpression {
//        if (minBP != 0) {advance()}  // Advance first if it's called by itself
//        skipSpace()
//
//        var lhs: BaseExpression = if (currentToken.type == TokenType.L_PARAN) {
//            exprBracket()
//        } else {
//            val ctpos = currentToken.position
//            val ppos = ParsingPosition(pos, pos, ctpos, ctpos)
//            NumberExpr(currentToken, ppos)
//        }
//
//        while (true) {
//            val op = nextNonSpaceToken()
//            if (!(Constant.arithmeticOp + Constant.comparitiveOp).contains(op.type)) {break}
//
//            val (leftBP, rightBP) = Constant.bindingPower[op.type]!!
//            if (leftBP < minBP) {break}
//            ass()
//
//            val rhs = exprMath(rightBP)
//            val ppos = ParsingPosition(lhs.position.start, rhs.position.end,
//                                       lhs.position.startTokenPosition, rhs.position.endTokenPosition)
//            lhs = MathExpr(op, lhs, rhs, ppos)
//        }
//        return lhs
//    }
//
//    private fun stmBracket(): BracketStm {
//        val bracketTypeL = currentToken.type
//        val bracketTypeR = Constant.bracket[bracketTypeL]!!
//        var paranCount = 1
//        val start = pos
//        val startToken = currentToken
//
//        while (paranCount > 0) {  // Find the matching closing bracket in terms of number
//            advance()
//            val tt = currentToken.type
//            if (tt == TokenType.EOF) {throw ParsingException("Script ended with unclosed bracket", startToken.position)}
//            else if (Constant.bracket.keys.contains(tt)) {paranCount++}
//            else if (Constant.bracket.values.contains(tt)) {paranCount--}
//        }
//
//        if (currentToken.type != bracketTypeR) {  // Confirm the "matching" bracket is the same type
//            val ctpos = currentToken.position
//            val pos = LexingPosition(ctpos.filename, ctpos.lineNumber, ctpos.start, ctpos.end)
//            throw ParsingException("Expected closing bracket ${bracketTypeR.name} and found ${currentToken.type}", pos)
//        }
//
//        val end = pos
//        val endToken = currentToken
//        val eofPosition = LexingPosition(endToken.position.filename, endToken.position.lineNumber,
//            endToken.position.end - 1, endToken.position.end - 1)
//
//        val innerTokens = tokens.subList(start + 1, end) + Token("EOF", eofPosition)
//
//        val innerStm = if (innerTokens.isNotEmpty()) Parser(innerTokens).parse() else NullExpr()
//
//        val ppos = ParsingPosition(start, end, startToken.position, endToken.position)
//        return BracketStm(innerStm, startToken, endToken, ppos)
//    }
//
//    private fun exprBracket(): BaseExpression {
//        val bracStm = stmBracket()
//
//        if (bracStm.stm !is BaseExpression) { throw ParsingException("Expected expression inside bracket", bracStm.position.startTokenPosition) }
//
//        return BracketExpr(bracStm.stm, bracStm.bracL, bracStm.bracR, bracStm.position)
//    }
//
//    private fun stmIf(): IfStm {
//        val start = pos
//        val startToken = currentToken
//
//        ass()
//        val mainCondition = exprBracket()  // if (...) {
//        ass()
//        val mainAction = stmBracket()  // { ... }
//
//        val elif = mutableMapOf<BaseExpression, BaseStatement>()
//        ass()
//        while (currentToken.type == TokenType.ELIF) {
//            val condition = exprBracket()  // elif (...) {
//            ass()
//            val action = stmBracket()  // { ... }
//            elif[condition] = action
//            ass()
//        }
//
//        var elseAction: BaseStatement = NullExpr()
//        if (currentToken.type == TokenType.ELSE) {
//            gotoNext(TokenType.L_BRACE)
//            elseAction = stmBracket()  // { ... }
//            ass()
//        }
//
//        val end = pos
//        val endToken = currentToken
//
//        val ppos = ParsingPosition(start, end, startToken.position, endToken.position)
//        return IfStm(mainCondition, mainAction, elif, elseAction, ppos)
//    }
//
//    private fun stmLoop(): LoopStm {  // Matches both for and while because of their similar structures
//        val start = pos
//        val startToken = currentToken
//
//        ass()
//        val condition = exprBracket()  // while (...) {
//        ass()
//        val mainAction = stmBracket()  // { ... }
//
//        ass()
//        var completeAction: BaseStatement = NullExpr()
//        var incompleteAction: BaseStatement = NullExpr()
//        while (listOf(TokenType.COMPLETE, TokenType.INCOMPLETE).contains(currentToken.type)) {
//            val tokenType = currentToken.type
//            gotoNext(TokenType.L_BRACE)
//            val action = stmBracket()  // { ... }
//            if (tokenType == TokenType.COMPLETE) {
//                if (completeAction !is NullExpr) throw ParsingException("While/For block following with multiple complete actions", currentToken.position)
//                completeAction = action
//            } else {
//                if (incompleteAction !is NullExpr) throw ParsingException("While/For block following with multiple incomplete actions", currentToken.position)
//                incompleteAction = action
//            }
//
//            ass()
//        }
//
//        val end = pos
//        val endToken = currentToken
//
//        val ppos = ParsingPosition(start, end, startToken.position, endToken.position)
//        return LoopStm(startToken.type, condition, mainAction, completeAction, incompleteAction, ppos)
//    }
}