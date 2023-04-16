package com.vinkami.vinkamilang.language.parse

import com.vinkami.vinkamilang.language.Constant
import com.vinkami.vinkamilang.language.exception.*
import com.vinkami.vinkamilang.language.lex.Position
import com.vinkami.vinkamilang.language.lex.Token
import com.vinkami.vinkamilang.language.lex.TokenType
import com.vinkami.vinkamilang.language.parse.node.*


class Parser(private val tokens: List<Token>) {
    private var pos = -1

    private val currentToken: Token
        get() = tokens[pos]
    private val currentType: TokenType
        get() = currentToken.type
    private val currentStartPos: Position
        get() = currentToken.startPos
    private val currentEndPos: Position
        get() = currentToken.endPos

    private val nextNonSpaceToken: Token
        get() = tokens.subList(pos + 1, tokens.size).firstOrNull { it.type !in listOf(TokenType.SPACE, TokenType.LINEBREAK) } ?: tokens.last()
    private val nextType: TokenType
        get() = nextNonSpaceToken.type

    init {advance()}

    private fun advance(): BaseError? {
        if (pos == tokens.size - 1) return SyntaxError("Unexpected end of file", currentStartPos, currentEndPos)
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
        val startPos = currentStartPos

        try {
            skipSpace()
            while (true) {
                procedures += res(parseOnce()).node  // throws UPAE if currentResult has error
                if (currentType == TokenType.EOF) break
                ass()
            }
        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }
        val endPos = currentEndPos

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
            else -> ParseResult(SyntaxError("Unexpected token $currentType", currentStartPos, currentEndPos))
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
            else -> ParseResult(SyntaxError("Unexpected token $currentType", currentStartPos, currentEndPos))
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
                else -> throw SyntaxError("Unexpected token $currentType", currentStartPos, currentEndPos)
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
                    else if (nextType != TokenType.COMMA) throw SyntaxError("No comma seperating arguments", currentStartPos, currentEndPos)
                    else ass()
                }
                ass()
            }

            res(IdenNode(nameToken, args, kwargs, withCall, currentEndPos))
        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }

        return res
    }

    private fun parseAssign(): ParseResult {
        val res = ParseResult()
        val startPos = currentStartPos

        try {
            ass()  // skip var token
            if (currentType != TokenType.IDENTIFIER) throw SyntaxError("Expected identifier after var", currentStartPos, currentEndPos)
            val iden = res(parseIden()).node as IdenNode
            ass()

            if (currentType !in Constant.difinitiveOp) throw SyntaxError("Expected assignment operator after identifier", currentStartPos, currentEndPos)
            val assignToken = currentToken
            ass()

            if (currentType in listOf(TokenType.EOF, TokenType.LINEBREAK)) throw SyntaxError("Unexpected end of line", currentStartPos, currentEndPos)
            val value = res(parseOnce()).node
            res(AssignNode(iden, assignToken, value, startPos))
        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }

        return res
    }

    private fun parseBracket(): ParseResult {
        val res = ParseResult()

        try {
            val bracketTypeL = currentType
            if (bracketTypeL !in Constant.bracket.keys) throw NotYourFaultError("Illegal bracket type $bracketTypeL", currentStartPos, currentEndPos)
            val bracketTypeR = Constant.bracket[bracketTypeL]

            var paranCount = 1
            val start = pos
            val startToken = currentToken

            while (paranCount > 0) {  // Find the matching closing bracket in terms of number
                advance()
                val tt = currentType
                if (tt == TokenType.EOF) throw SyntaxError("Script ended when expecting a $bracketTypeR", currentStartPos, currentEndPos)
                else if (Constant.bracket.keys.contains(tt)) paranCount++
                else if (Constant.bracket.values.contains(tt)) paranCount--
            }

            // Confirm the "matching" bracket is the same type
            if (currentType != bracketTypeR) throw SyntaxError("Expected $bracketTypeR, got $currentType", currentStartPos, currentEndPos)

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
        val startPos = currentStartPos

        try {
            ass()
            val mainCond = res(parseBracket()).node
            ass()
            val mainAction = res(parseBracket()).node
            var endPos = currentEndPos

            ass()
            val elif = mutableMapOf<BaseNode, BaseNode>()
            while (currentType == TokenType.ELIF) {
                ass()
                val cond = res(parseBracket()).node
                ass()
                val action = res(parseBracket()).node

                endPos = currentEndPos
                elif[cond] = action
                ass()
            }

            var elseAction: BaseNode? = null
            if (currentType == TokenType.ELSE) {
                ass()
                elseAction = res(parseBracket()).node
                endPos = currentEndPos
            }

            return res(IfNode(mainCond, mainAction, elif, elseAction, startPos, endPos))

        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }
    }

    private fun parseLoop(): ParseResult {
        val res = ParseResult()
        val startPos = currentStartPos
        val loopTT = currentType

        try {
            ass()
            val condition = res(parseBracket()).node
            ass()
            val mainAction = res(parseBracket()).node
            var endPos = currentEndPos

            var compAction: BaseNode? = null
            var incompAction: BaseNode? = null
            while (nextType in Constant.loopCompleteTT) {
                ass()
                val tt = currentType
                ass()
                when (tt) {
                    TokenType.COMPLETE -> compAction = res(parseBracket()).node
                    TokenType.INCOMPLETE -> incompAction = res(parseBracket()).node
                    else -> throw NotYourFaultError("Illegal loop complete type $tt", currentStartPos, currentEndPos)
                }
                endPos = currentEndPos
            }

            return res(LoopNode(loopTT, condition, mainAction, compAction, incompAction, startPos, endPos))

        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }
    }

    private fun parseFuncDef(): ParseResult {
        val res = ParseResult()
        val startPos = currentStartPos
        var params = listOf<ParamNode>()
        var returnType: IdenNode? = null

        try {
            ass()

            val name = currentToken
            ass()

            if (currentType != TokenType.L_PARAN) throw SyntaxError("Expected ( after function name", currentStartPos, currentEndPos)

            if (nextType != TokenType.R_PARAN) {
                // Has params
                ass()
                params = generateParams(res)
                paramCheck(params)
            }

            ass()
            if (nextType == TokenType.COLON) {
                // Has returnType
                ass()
                if (nextType != TokenType.IDENTIFIER) throw SyntaxError("Expected return type after colon", currentStartPos, currentEndPos)
                ass()
                returnType = res(parseIden()).node as IdenNode
            }

            if (nextType != TokenType.L_BRACE) throw SyntaxError("Expected { after function declaration", currentStartPos, currentEndPos)
            ass()
            val body = res(parseBracket()).node

            return res(FuncNode(name, params, returnType, body, startPos, currentEndPos))
        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }
    }

    private fun parseClass(): ParseResult {
        val res = ParseResult()
        val startPos = currentStartPos
        var params = listOf<ParamNode>()
        var parent: IdenNode? = null

        try {
            ass()
            val name = currentToken
            ass()

            // has constructor params
            if (currentType == TokenType.L_PARAN) {
                ass()
                if (currentType == TokenType.IDENTIFIER) {
                    params = generateParams(res)

                    if (nextType != TokenType.R_PARAN) throw SyntaxError("Unclosed bracket", currentStartPos, currentEndPos)
                    ass(2)
                } else if (currentType != TokenType.R_PARAN) throw SyntaxError("Unclosed bracket", currentStartPos, currentEndPos)
                else ass()  // skip useless R_PARAN

                paramCheck(params)
            }

            // has inheritance
            if (currentType == TokenType.COLON) {
                ass()
                if (currentType != TokenType.IDENTIFIER) throw SyntaxError("Expect class name after colon", currentStartPos, currentEndPos)
                parent = res(parseIden()).node as IdenNode
                ass()
            }

            if (currentType != TokenType.L_BRACE) throw SyntaxError("Expected { after class declaration", currentStartPos, currentEndPos)
            val body = res(parseBracket()).node

            res(ClassNode(name, params, parent, body, startPos, currentEndPos))
        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }

        return res
    }

    private fun generateParams(res: ParseResult): List<ParamNode> {
        val params = mutableListOf<ParamNode>()
        params += res(parseParam()).node as ParamNode

        while (nextType == TokenType.COMMA) {
            ass()
            if (nextType !in listOf(TokenType.IDENTIFIER, TokenType.MULTIPLY, TokenType.POWER)) throw SyntaxError("Expected parameter name after comma", currentStartPos, currentEndPos)
            ass()
            params += res(parseParam()).node as ParamNode
        }
        return params
    }

    private fun paramCheck(params: List<ParamNode>) {
        if (params.find { it.variable } != params.findLast { it.variable }) throw SyntaxError("More than 1 variable arguments", currentStartPos, currentEndPos)
        if (params.find { it.kwvariable } != params.findLast { it.kwvariable }) throw SyntaxError("More than 1 variable keyword arguments", currentStartPos, currentEndPos)

        if (params.subList(params.indexOfFirst { it.default != null }, params.size).find { it.default == null } != null) throw SyntaxError("Paramaters with default values must be after those without default values", currentStartPos, currentEndPos)
    }

    private fun parseParam(): ParseResult {
        val res = ParseResult()
        var variable = false
        var kwvariable = false

        try {
            if (currentType == TokenType.MULTIPLY) { variable = true; ass() }
            else if (currentType == TokenType.POWER) { kwvariable = true; ass() }

            if (currentType != TokenType.IDENTIFIER) throw SyntaxError("Illegal character", currentStartPos, currentEndPos)

            val name = res(parseIden()).node as IdenNode
            var type: IdenNode? = null
            var default: BaseNode? = null

            if (nextType == TokenType.COLON) {
                ass()
                if (nextType != TokenType.IDENTIFIER) throw SyntaxError("Expected type after :", currentStartPos, currentEndPos)
                ass()
                type = res(parseIden()).node as IdenNode
            }

            if (nextType == TokenType.ASSIGN) {
                ass(2)
                default = res(parseOnce()).node
            }

            res(ParamNode(name, type, default, currentEndPos, variable, kwvariable))
        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }

        return res
    }

    private fun parseProp(node: BaseNode): ParseResult {
        val res = ParseResult()

        try {
            advance()
            advance()
            if (currentType != TokenType.IDENTIFIER) throw SyntaxError("Expected property name after .", currentStartPos, currentEndPos)

            val prop = res(parseOnce()).node as IdenNode

            res(PropAccessNode(node, prop))
        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }

        return res
    }
}