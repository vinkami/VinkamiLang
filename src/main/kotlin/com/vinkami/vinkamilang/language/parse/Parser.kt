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
    private val currentType: TokenType
        get() = currentToken.type

    private val nextNonSpaceToken: Token
        get() = tokens.subList(pos + 1, tokens.size).firstOrNull { it.type !in listOf(TokenType.SPACE, TokenType.LINEBREAK) } ?: tokens.last()
    private val nextType: TokenType
        get() = nextNonSpaceToken.type

    init {advance()}

    private fun advance(): BaseError? {
        if (pos == tokens.size - 1) return SyntaxError("Unexpected end of file", currentToken.startPos, currentToken.endPos)
        pos++
        return null
    }

    private fun skipSpace(): BaseError? {
        while (listOf(TokenType.SPACE, TokenType.LINEBREAK).contains(currentType)) advance().let { if (it != null) return it }
        return null
    }

    /**
     * Advance, Skip Space
     */
    private tailrec fun ass(n: Int = 1): BaseError? {
        advance().let { if (it != null) return it }
        skipSpace().let { if (it != null) return it }
        return if (n == 1) null else ass(n-1)
    }

    fun parse(): ParseResult {
        val res = ParseResult()
        val procedures = mutableListOf<BaseNode>()
        val startPos = currentToken.startPos

        try {
            skipSpace()
            while (true) {
                procedures += res(parseOnce()).node  // throws UPAE if currentResult has error
                if (currentType == TokenType.EOF) break
                ass()
            }
        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }
        val endPos = currentToken.endPos

        if (procedures.size == 1 || procedures.size == 2) return res(procedures[0])  // 1: Only NullNode from EOF; 2: Only one procedure
        return res(ProcedureNode(procedures, startPos, endPos))
    }

    private fun parseOnce(): ParseResult {
        val res = ParseResult()

        var currentProcedure = res(when (currentType) {
            TokenType.NUMBER, TokenType.IDENTIFIER -> parseBinOp(0)
            TokenType.STRING -> ParseResult(StringNode(currentToken))
            TokenType.PLUS, TokenType.MINUS -> parseUnaryOp()
            TokenType.VAR -> parseAssign()
            in Constant.bracket.keys -> parseBracket()
            TokenType.IF -> parseIf()
            TokenType.WHILE, TokenType.FOR -> parseLoop()
            TokenType.FUNC -> parseFuncDef()
            TokenType.CLASS -> parseClass()
            TokenType.EOF -> ParseResult(NullNode(currentToken))
            TokenType.UNKNOWN -> ParseResult(IllegalCharError(currentToken))
            else -> ParseResult(SyntaxError("Unexpected token $currentType", currentToken.startPos, currentToken.endPos))
        }).node

        while (true) {  // immediate next token instead of next non-space
            if (pos == tokens.size - 1) break
            if (tokens[pos+1].type != TokenType.DOT) break
            currentProcedure = res(parseProp(currentProcedure)).node  // gets property
        }

        return res(currentProcedure)
    }

    private fun parseExprOnce(): ParseResult { // Only expressions, not statements
        return when (currentType) {
            TokenType.NUMBER -> ParseResult(NumberNode(currentToken))
            TokenType.IDENTIFIER -> parseIden()
            TokenType.STRING -> ParseResult(StringNode(currentToken))
            TokenType.PLUS, TokenType.MINUS -> parseUnaryOp()
            in Constant.bracket.keys -> parseBracket()
            else -> ParseResult(SyntaxError("Unexpected token $currentType", currentToken.startPos, currentToken.endPos))
        }
    }

    /**
     * Binary Operations
     * i.e. + - * / % ** == != < > <= >= and stuff like that
     *
     * @param minBP Minimum binding power; should be 0 when called from outside and varies according to Constant.bindingPower when called by processBinOp()
     */
    private fun parseBinOp(minBP: Int): ParseResult {
        val res = ParseResult()

        try {
            if (minBP != 0) advance()
            skipSpace()

            val lhs: BaseNode = when (currentType) {
                TokenType.L_PARAN -> res(parseBracket()).node
                TokenType.NUMBER -> NumberNode(currentToken)
                TokenType.IDENTIFIER -> res(parseIden()).node
                else -> throw SyntaxError("Unexpected token $currentType", currentToken.startPos, currentToken.endPos)
            }

            res(processBinOp(minBP, res, lhs))

        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }

        return res
    }

    /**
     * Actual Pratt parsing part of parseBinOp()
     * Used by parseBracket() and parseBinOp() so it's broken down into a separate function
     *
     * @param minBP see parseBinOp()
     * @param res the parseResult from the caller
     * @param currentNode the node to start parsing at
     */
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
            val inner = res(parseExprOnce())
            res(processBinOp(0, res, UnaryOpNode(op, inner.node)))  // unary node may be a part of math expression
        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }

        return res
    }

    private fun parseIden(): ParseResult {
        val res = ParseResult()

        try {
            val nameToken = currentToken
            val withCall = tokens[pos+1].type == TokenType.L_PARAN  // not simply (nextType == TokenType.L_PARAN) because of space
            val args = mutableListOf<BaseNode>()
            val kwargs = mutableMapOf<Token, BaseNode>()

            if (withCall) {
                ass()
                var argsEnd = false
                var paramsEnd = nextType == TokenType.R_PARAN

                while (!paramsEnd) {
                    ass()
                    if (!argsEnd) {
                        if (nextType == TokenType.ASSIGN) {  // treat this and all future params as kwargs
                            argsEnd = true
                            val name = currentToken
                            ass(2)
                            kwargs[name] = res(parseOnce()).node
                        } else {
                            args += res(parseOnce()).node
                        }

                    } else {  // kwargs
                        val name = currentToken
                        ass()
                        kwargs[name] = res(parseOnce()).node
                    }

                    if (nextType == TokenType.R_PARAN) paramsEnd = true
                    else if (nextType != TokenType.COMMA) throw SyntaxError("No comma seperating arguments", currentToken.startPos, currentToken.endPos)
                    else ass()
                }
                ass()
            }

            res(IdenNode(nameToken, args, kwargs, withCall, currentToken.endPos))
        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }

        return res
    }

    private fun parseAssign(): ParseResult {
        val res = ParseResult()
        val startPos = currentToken.startPos

        try {
            ass()  // skip var token
            if (currentType != TokenType.IDENTIFIER) throw SyntaxError("Expected identifier after var", currentToken.startPos, currentToken.endPos)
            val iden = res(parseIden()).node as IdenNode
            ass()

            if (currentType !in Constant.difinitiveOp) throw SyntaxError("Expected assignment operator after identifier", currentToken.startPos, currentToken.endPos)
            val assignToken = currentToken
            ass()

            if (currentType in listOf(TokenType.EOF, TokenType.LINEBREAK)) throw SyntaxError("Unexpected end of line", currentToken.startPos, currentToken.endPos)
            val value = res(parseOnce()).node
            res(AssignNode(iden, assignToken, value, startPos))
        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }

        return res
    }

    private fun parseBracket(): ParseResult {
        val res = ParseResult()

        try {
            val bracketTypeL = currentType
            if (bracketTypeL !in Constant.bracket.keys) throw NotYourFaultError("Illegal bracket type $bracketTypeL", currentToken.startPos, currentToken.endPos)
            val bracketTypeR = Constant.bracket[bracketTypeL]

            var paranCount = 1
            val start = pos
            val startToken = currentToken

            while (paranCount > 0) {  // Find the matching closing bracket in terms of number
                advance()
                val tt = currentType
                if (tt == TokenType.EOF) throw SyntaxError("Script ended when expecting a $bracketTypeR", currentToken.startPos, currentToken.endPos)
                else if (Constant.bracket.keys.contains(tt)) paranCount++
                else if (Constant.bracket.values.contains(tt)) paranCount--
            }

            // Confirm the "matching" bracket is the same type
            if (currentType != bracketTypeR) throw SyntaxError("Expected $bracketTypeR, got $currentType", currentToken.startPos, currentToken.endPos)

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
            while (currentType == TokenType.ELIF) {
                ass()
                val cond = res(parseBracket()).node
                ass()
                val action = res(parseBracket()).node

                endPos = currentToken.endPos
                elif[cond] = action
                ass()
            }

            var elseAction: BaseNode? = null
            if (currentType == TokenType.ELSE) {
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
        val loopTT = currentType

        try {
            ass()
            val condition = res(parseBracket()).node
            ass()
            val mainAction = res(parseBracket()).node
            var endPos = currentToken.endPos

            var compAction: BaseNode? = null
            var incompAction: BaseNode? = null
            while (nextType in Constant.loopCompleteTT) {
                ass()
                val tt = currentType
                ass()
                when (tt) {
                    TokenType.COMPLETE -> compAction = res(parseBracket()).node
                    TokenType.INCOMPLETE -> incompAction = res(parseBracket()).node
                    else -> throw NotYourFaultError("Illegal loop complete type $tt", currentToken.startPos, currentToken.endPos)
                }
                endPos = currentToken.endPos
            }

            return res(LoopNode(loopTT, condition, mainAction, compAction, incompAction, startPos, endPos))

        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }
    }

    private fun parseFuncDef(): ParseResult {
        val res = ParseResult()
        val startPos = currentToken.startPos
        val params = mutableListOf<ParamNode>()
        var returnType: IdenNode? = null

        try {
            ass()

            val name = currentToken
            ass()

            if (currentType != TokenType.L_PARAN) throw SyntaxError("Expected ( after function name", currentToken.startPos, currentToken.endPos)

            if (nextType != TokenType.R_PARAN) {
                ass()
                // Has params
                params += res(parseParam()).node as ParamNode

                while (nextType == TokenType.COMMA) {
                    ass()
                    if (nextType != TokenType.IDENTIFIER) throw SyntaxError("Expected parameter name after comma", currentToken.startPos, currentToken.endPos)
                    ass()
                    params += res(parseParam()).node as ParamNode
                }
            }

            ass()
            if (nextType == TokenType.COLON) {
                // Has returnType
                ass()
                if (nextType != TokenType.IDENTIFIER) throw SyntaxError("Expected return type after colon", currentToken.startPos, currentToken.endPos)
                ass()
                returnType = res(parseIden()).node as IdenNode
            }

            if (nextType != TokenType.L_BRACE) throw SyntaxError("Expected { after function declaration", currentToken.startPos, currentToken.endPos)
            ass()
            val body = res(parseBracket()).node

            return res(FuncNode(name, params, returnType, body, startPos, currentToken.endPos))
        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }
    }

    private fun parseClass(): ParseResult {
        val res = ParseResult()
        val startPos = currentToken.startPos
        val params = mutableListOf<ParamNode>()
        var parent: IdenNode? = null

        try {
            ass()
            val name = currentToken
            ass()

            // has constructor params
            if (currentType == TokenType.L_PARAN) {
                ass()
                if (currentType == TokenType.IDENTIFIER) {
                    params += res(parseParam()).node as ParamNode

                    while (nextType == TokenType.COMMA) {
                        ass()
                        if (nextType != TokenType.IDENTIFIER) throw SyntaxError("Expected parameter name after comma", currentToken.startPos, currentToken.endPos)
                        ass()
                        params += res(parseParam()).node as ParamNode
                    }
                    if (nextType != TokenType.R_PARAN) throw SyntaxError("Unclosed bracket", currentToken.startPos, currentToken.endPos)
                    ass(2)
                } else if (currentType != TokenType.R_PARAN) throw SyntaxError("Unclosed bracket", currentToken.startPos, currentToken.endPos)
                else ass()  // skip useless R_PARAN
            }

            // has inheritance
            if (currentType == TokenType.COLON) {
                ass()
                if (currentType != TokenType.IDENTIFIER) throw SyntaxError("Expect class name after colon", currentToken.startPos, currentToken.endPos)
                parent = res(parseIden()).node as IdenNode
                ass()
            }

            if (currentType != TokenType.L_BRACE) throw SyntaxError("Expected { after class declaration", currentToken.startPos, currentToken.endPos)
            val body = res(parseBracket()).node

            return res(ClassNode(name, params, parent, body, startPos, currentToken.endPos))
        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }
    }

    private fun parseParam(): ParseResult {
        val res = ParseResult()

        try {
            val name = res(parseIden()).node as IdenNode
            var type: IdenNode? = null
            var default: BaseNode? = null

            if (nextType == TokenType.COLON) {
                ass()
                if (nextType != TokenType.IDENTIFIER) throw SyntaxError("Expected type after :", currentToken.startPos, currentToken.endPos)
                ass()
                type = res(parseIden()).node as IdenNode
            }

            if (nextType == TokenType.ASSIGN) {
                ass(2)
                default = res(parseOnce()).node
            }

            return res(ParamNode(name, type, default, currentToken.endPos))
        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }
    }

    private fun parseProp(node: BaseNode): ParseResult {
        val res = ParseResult()

        try {
            advance()
            advance()
            if (currentType != TokenType.IDENTIFIER) throw SyntaxError("Expected property name after .", currentToken.startPos, currentToken.endPos)

            val prop = res(parseOnce()).node as IdenNode

            return res(PropAccessNode(node, prop))
        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }
    }
}