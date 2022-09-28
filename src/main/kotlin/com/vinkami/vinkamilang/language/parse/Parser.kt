package com.vinkami.vinkamilang.language.parse

import com.vinkami.vinkamilang.language.Constant
import com.vinkami.vinkamilang.language.exception.BaseLangException
import com.vinkami.vinkamilang.language.exception.IllegalCharError
import com.vinkami.vinkamilang.language.exception.NotYourFaultError
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
            TokenType.NUMBER, TokenType.L_PARAN -> parseBinOp(0)
            TokenType.PLUS, TokenType.MINUS -> parseUnaryOp()
            in Constant.bracket.keys -> parseBracket()
            TokenType.IF -> parseIf()
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

    private fun parseBinOp(minBP: Int): ParseResult {
        val res = ParseResult()

        try {
            if (minBP != 0) advance()
            skipSpace()

            var lhs: BaseNode = if (currentToken.type == TokenType.L_PARAN) {
                val brac = res(parseBracket())
                brac.node
            } else {
                NumberNode(currentToken)
            }

            while (true) {
                val op = nextNonSpaceToken()
                if (!(Constant.arithmeticOp + Constant.comparitiveOp).contains(op.type)) break

                val (leftBP, rightBP) = Constant.bindingPower[op.type]!!
                if (leftBP < minBP) {break}
                ass()

                val rhs = res(parseBinOp(rightBP)).node
                lhs = BinOpNode(lhs, op, rhs)
            }

            return res(lhs)
        } catch (e: BaseLangException) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }
    }

    private fun parseUnaryOp(): ParseResult {
        val res = ParseResult()

        return try {
            val op = currentToken
            ass()
            val node = res(parse())
            res(UnaryOpNode(op, node.node))
        } catch (e: BaseLangException) { res(e) } catch (e: UninitializedPropertyAccessException) { res }
    }

    private fun parseBracket(): ParseResult {
        val res = ParseResult()

        try {
            val bracketTypeL = currentToken.type
            if (bracketTypeL !in Constant.bracket.keys) throw NotYourFaultError("Illegal bracket type $bracketTypeL", currentToken.startPos, currentToken.endPos)
            val bracketTypeR = Constant.bracket[bracketTypeL]

            var paranCount = 1
            val start = pos
            val startToken = currentToken

            while (paranCount > 0) {  // Find the matching closing bracket in terms of number
                advance()
                val tt = currentToken.type
                if (tt == TokenType.EOF) throw SyntaxError("Script ended when expecting a $bracketTypeR", currentToken.startPos, currentToken.endPos)
                else if (Constant.bracket.keys.contains(tt)) paranCount++
                else if (Constant.bracket.values.contains(tt)) paranCount--
            }

            // Confirm the "matching" bracket is the same type
            if (currentToken.type != bracketTypeR) throw SyntaxError("Expected $bracketTypeR, got ${currentToken.type}", currentToken.startPos, currentToken.endPos)

            val endToken = currentToken
            val eof = Token(TokenType.EOF, "EOF", endToken.startPos, endToken.endPos)
            val innerTokens = tokens.subList(start + 1, pos) + eof
            val innerResult = res(if (innerTokens.isNotEmpty()) Parser(innerTokens).parse() else ParseResult(NullNode(currentToken)))

            return res(BracketNode(startToken, innerResult.node, endToken))

        } catch (e: BaseLangException) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }
    }

    private fun parseIf(): ParseResult {
        val res = ParseResult()
        val startPos = currentToken.startPos

        try {
            ass()
            val mainCond = res(parseBracket()).node
            ass()
            val mainAction = res(parseBracket()).node
            var endPos = currentToken.endPos

            ass()
            val elif = mutableMapOf<BaseNode, BaseNode>()
            while (currentToken.type == TokenType.ELIF) {
                ass()
                val cond = res(parseBracket()).node
                ass()
                val action = res(parseBracket()).node

                endPos = currentToken.endPos
                elif[cond] = action
                ass()
            }

            var elseAction: BaseNode? = null
            if (currentToken.type == TokenType.ELSE) {
                ass()
                elseAction = res(parseBracket()).node
                endPos = currentToken.endPos
            }

            return res(IfNode(mainCond, mainAction, elif, elseAction, startPos, endPos))

        } catch (e: BaseLangException) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }
    }



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