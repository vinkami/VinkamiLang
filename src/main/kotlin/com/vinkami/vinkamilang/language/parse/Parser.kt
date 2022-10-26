package com.vinkami.vinkamilang.language.parse

import com.vinkami.vinkamilang.language.Constant
import com.vinkami.vinkamilang.language.exception.*
import com.vinkami.vinkamilang.language.lex.Token
import com.vinkami.vinkamilang.language.lex.TokenType
import com.vinkami.vinkamilang.language.parse.node.*


class Parser(private val tokens: List<Token>) {
    private var pos = -1
    private val currentToken: Token
        get() = tokens[pos]
    private val nextNonSpaceToken: Token
        get() = tokens.subList(pos + 1, tokens.size).firstOrNull { it.type != TokenType.SPACE } ?: tokens.last()

    init {advance()}

    private fun advance(): BaseError? {
        if (pos  == tokens.size - 1) {
            return SyntaxError("Unexpected end of file", currentToken.startPos, currentToken.endPos)
        }
        pos++
        return null
    }

    private fun skipSpace(): BaseError? {
        while (listOf(TokenType.SPACE, TokenType.LINEBREAK).contains(currentToken.type)) {
            advance().let {if (it != null) return it}
        }
        return null
    }

    private fun ass(): BaseError? {
        advance().let { if (it != null) return it }
        skipSpace().let { if (it != null) return it }
        return null
    }

    fun parse(): ParseResult {
        val res = ParseResult()
        val procedures = mutableListOf<BaseNode>()
        val startPos = currentToken.startPos

        try {
            skipSpace()
            while (true) {
                procedures += res(parseOnce()).node  // throws UPAE if currentResult has error

                if (currentToken.type == TokenType.EOF) break
                ass()
            }
        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }
        val endPos = currentToken.endPos

        if (procedures.size == 1 || procedures.size == 2) return res(procedures[0])  // 1: Only NullNode from EOF; 2: Only one procedure
        return res(ProcedralNode(procedures.dropLast(1), startPos, endPos))  // TODO: Final procedure seems to be dropped
    }

    private fun parseOnce(): ParseResult {
        return when (currentToken.type) {
            TokenType.NUMBER, TokenType.IDENTIFIER -> parseBinOp(0)
            TokenType.STRING -> ParseResult(StringNode(currentToken))
            TokenType.PLUS, TokenType.MINUS -> parseUnaryOp()
            TokenType.VAR -> parseAssign()
            in Constant.bracket.keys -> parseBracket()
            TokenType.IF -> parseIf()
            TokenType.WHILE, TokenType.FOR -> parseLoop()
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

            val lhs: BaseNode = when (currentToken.type) {
                TokenType.L_PARAN -> res(parseBracket()).node
                TokenType.NUMBER -> NumberNode(currentToken)
                TokenType.IDENTIFIER -> res(parseIden()).node
                else -> throw SyntaxError("Unexpected token ${currentToken.type}", currentToken.startPos, currentToken.endPos)
            }

            res(processBinOp(minBP, res, lhs))

        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }

        return res
    }

    private fun processBinOp(minBP: Int, res: ParseResult, currentNode: BaseNode): BaseNode {
        var lhs = currentNode

        while (true) {
            val op = nextNonSpaceToken
            if (op.type !in Constant.operators) break

            val (leftBP, rightBP) = Constant.bindingPower[op.type]!!
            if (leftBP < minBP) break
            ass()

            val rhs = res(parseBinOp(rightBP)).node
            lhs = BinOpNode(lhs, op, rhs)
        }

        return lhs
    }

    private fun parseUnaryOp(): ParseResult {
        val res = ParseResult()

        try {
            val op = currentToken
            ass()
            val node = res(parse())
            res(UnaryOpNode(op, node.node))
        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }

        return res
    }

    private fun parseIden(): ParseResult {
        val res = ParseResult()

        try {
            val node = IdenNode(currentToken)
            res(node)
        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }

        return res
    }

    private fun parseAssign(): ParseResult {
        val res = ParseResult()
        val startPos = currentToken.startPos

        try {
            ass()  // skip var token
            if (currentToken.type != TokenType.IDENTIFIER) throw SyntaxError("Expected identifier after var", currentToken.startPos, currentToken.endPos)
            val iden = res(parseIden()).node as IdenNode
            ass()

            if (currentToken.type !in Constant.difinitiveOp) throw SyntaxError("Expected assignment operator after identifier", currentToken.startPos, currentToken.endPos)
            val assignToken = currentToken
            ass()

            if (currentToken.type in listOf(TokenType.EOF, TokenType.LINEBREAK)) throw SyntaxError("Unexpected end of line", currentToken.startPos, currentToken.endPos)
            val value = res(parseOnce()).node
            res(AssignNode(iden, assignToken, value, startPos))
        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }

        return res
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
            if (innerResult.hasError) throw innerResult.error

            val node = BracketNode(startToken, innerResult.node, endToken)
            return res(
                if (bracketTypeL == TokenType.L_PARAN) processBinOp(0, res, node)  // Try to continue parsing the bracket as a binop, return itself if not anyway
                else node
            )

        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }
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

        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }
    }

    private fun parseLoop(): ParseResult {
        val res = ParseResult()
        val startPos = currentToken.startPos
        val loopTT = currentToken.type

        try {
            ass()
            val condition = res(parseBracket()).node
            ass()
            val mainAction = res(parseBracket()).node
            var endPos = currentToken.endPos

            ass()
            var compAction: BaseNode? = null
            var incompAction: BaseNode? = null
            while (currentToken.type in Constant.loopCompleteTT) {
                val tt = currentToken.type
                ass()
                when (tt) {
                    TokenType.COMPLETE -> compAction = res(parseBracket()).node
                    TokenType.INCOMPLETE -> incompAction = res(parseBracket()).node
                    else -> throw NotYourFaultError("Illegal loop complete type $tt", currentToken.startPos, currentToken.endPos)
                }
                endPos = currentToken.endPos
                ass()
            }

            return res(LoopNode(loopTT, condition, mainAction, compAction, incompAction, startPos, endPos))

        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }
    }


// ------------ Legacy Code ------------
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