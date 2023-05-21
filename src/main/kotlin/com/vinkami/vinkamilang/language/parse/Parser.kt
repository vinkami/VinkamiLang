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

    private fun advance() {
        if (pos == tokens.size - 1) throw SyntaxError("Unexpected end of file", currentStartPos, currentEndPos)
        pos++
    }

    private fun skipSpace() {
        while (listOf(TokenType.SPACE, TokenType.LINEBREAK).contains(currentType)) advance()
    }

    /**
     * Advance, Skip Space
     */
    private tailrec fun ass(n: Int = 1) {
        advance()
        skipSpace()
        return if (n == 1) Unit else ass(n-1)
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

    private fun parseOnce(): BaseNode {
        var currentProcedure = when (currentType) {
            TokenType.NUMBER, TokenType.IDENTIFIER -> parseBinOp(0)
            TokenType.STRING -> StringNode(currentToken)
            TokenType.PLUS, TokenType.MINUS -> parseUnaryOp()
            TokenType.VAR -> parseAssign()
            in Constant.bracket.keys -> parseBracket()
            TokenType.IF -> parseIf()
            TokenType.WHILE, TokenType.FOR -> parseLoop()
            TokenType.FUNC -> parseFuncDef()
            TokenType.CLASS -> parseClass()
            TokenType.EOF -> NullNode(currentToken)
            TokenType.UNKNOWN -> throw IllegalCharError(currentToken)
            else -> throw SyntaxError("Unexpected token $currentType", currentStartPos, currentEndPos)
        }

        while (true) {  // immediate next token instead of next non-space
            if (pos == tokens.size - 1) break
            if (tokens[pos+1].type != TokenType.DOT) break
            currentProcedure = parseProp(currentProcedure)  // gets property
        }

        return currentProcedure
    }

    private fun parseExprOnce(): BaseNode { // Only expressions, not statements
        return when (currentType) {
            TokenType.NUMBER -> NumberNode(currentToken)
            TokenType.IDENTIFIER -> parseIden()
            TokenType.STRING -> StringNode(currentToken)
            TokenType.PLUS, TokenType.MINUS -> parseUnaryOp()
            in Constant.bracket.keys -> parseBracket()
            else -> throw SyntaxError("Unexpected token $currentType", currentStartPos, currentEndPos)
        }
    }

    /**
     * Binary Operations
     * i.e. + - * / % ** == != < > <= >= and stuff like that
     *
     * @param minBP Minimum binding power; should be 0 when called from outside and varies according to Constant.bindingPower when called by processBinOp()
     */
    private fun parseBinOp(minBP: Int): BaseNode {
        if (minBP != 0) advance()
        skipSpace()

        val lhs: BaseNode = when (currentType) {
            TokenType.L_PARAN -> parseBracket()
            TokenType.NUMBER -> NumberNode(currentToken)
            TokenType.IDENTIFIER -> parseIden()
            else -> throw SyntaxError("Unexpected token $currentType", currentStartPos, currentEndPos)
        }

        return processBinOp(minBP, lhs)
    }

    /**
     * Actual Pratt parsing part of parseBinOp()
     * Used by parseBracket() and parseBinOp() so it's broken down into a separate function
     *
     * @param minBP see parseBinOp()
     * @param currentNode the node to start parsing at
     */
    private fun processBinOp(minBP: Int, currentNode: BaseNode): BaseNode {
        var lhs = currentNode

        while (true) {
            val op = nextNonSpaceToken
            if (op.type !in Constant.operators) break

            val (leftBP, rightBP) = Constant.bindingPower[op.type]!!
            if (leftBP < minBP) break
            ass()

            val rhs = parseBinOp(rightBP)
            lhs = BinOpNode(lhs, op, rhs)
        }

        return lhs
    }

    private fun parseUnaryOp(): BaseNode {
        val op = currentToken
        ass()
        val inner = parseExprOnce()
        return processBinOp(0, UnaryOpNode(op, inner))  // unary node may be a part of math expression
    }

    private fun parseIden(): IdenNode {
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
                        kwargs[name] = parseOnce()
                    } else {
                        args += parseOnce()
                    }

                } else {  // kwargs
                    val name = currentToken
                    if (nextType != TokenType.ASSIGN) throw SyntaxError("No assign symbol in between", currentStartPos, currentEndPos)
                    ass(2)
                    kwargs[name] = parseOnce()
                }

                if (nextType == TokenType.R_PARAN) paramsEnd = true
                else if (nextType != TokenType.COMMA) throw SyntaxError("No comma seperating arguments", currentStartPos, currentEndPos)
                else ass()
            }
            ass()
        }

        return IdenNode(nameToken, args, kwargs, withCall, currentEndPos)
    }

    private fun parseAssign(): AssignNode {
        val startPos = currentStartPos

        ass()  // skip var token
        if (currentType != TokenType.IDENTIFIER) throw SyntaxError("Expected identifier after var", currentStartPos, currentEndPos)
        val iden = parseIden()
        ass()

        if (currentType !in Constant.difinitiveOp) throw SyntaxError("Expected assignment operator after identifier", currentStartPos, currentEndPos)
        val assignToken = currentToken
        ass()

        if (currentType in listOf(TokenType.EOF, TokenType.LINEBREAK)) throw SyntaxError("Unexpected end of line", currentStartPos, currentEndPos)
        val value = parseOnce()

        return AssignNode(iden, assignToken, value, startPos)
    }

    private fun parseBracket(): BaseNode {
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
        val innerResult = if (innerTokens.isNotEmpty()) Parser(innerTokens).parse().node else NullNode(currentToken)

        val node = BracketNode(startToken, innerResult, endToken)

        return if (bracketTypeL == TokenType.L_PARAN) processBinOp(0, node)  // Try to continue parsing the bracket as a binop, return itself if not anyway
        else node
    }

    private fun parseIf(): IfNode {
        val startPos = currentStartPos

        ass()
        val mainCond = parseBracket()
        ass()
        val mainAction = parseBracket()
        var endPos = currentEndPos

        ass()
        val elif = mutableMapOf<BaseNode, BaseNode>()
        while (currentType == TokenType.ELIF) {
            ass()
            val cond = parseBracket()
            ass()
            val action = parseBracket()

            endPos = currentEndPos
            elif[cond] = action
            ass()
        }

        var elseAction: BaseNode? = null
        if (currentType == TokenType.ELSE) {
            ass()
            elseAction = parseBracket()
            endPos = currentEndPos
        }

        return IfNode(mainCond, mainAction, elif, elseAction, startPos, endPos)
    }

    private fun parseLoop(): LoopNode {
        val startPos = currentStartPos
        val loopTT = currentType

        ass()
        val condition = parseBracket()
        ass()
        val mainAction = parseBracket()
        var endPos = currentEndPos

        var compAction: BaseNode? = null
        var incompAction: BaseNode? = null
        while (nextType in Constant.loopCompleteTT) {
            ass()
            val tt = currentType
            ass()
            when (tt) {
                TokenType.COMPLETE -> compAction = parseBracket()
                TokenType.INCOMPLETE -> incompAction = parseBracket()
                else -> throw NotYourFaultError("Illegal loop complete type $tt", currentStartPos, currentEndPos)
            }
            endPos = currentEndPos
        }

        return LoopNode(loopTT, condition, mainAction, compAction, incompAction, startPos, endPos)
    }

    private fun parseFuncDef(): FuncNode {
        val startPos = currentStartPos
        var params = listOf<ParamNode>()
        var returnType: IdenNode? = null

        ass()

        val name = currentToken
        ass()

        if (currentType != TokenType.L_PARAN) throw SyntaxError("Expected ( after function name", currentStartPos, currentEndPos)

        if (nextType != TokenType.R_PARAN) {
            // Has params
            ass()
            params = generateParams()
            paramCheck(params)
        }

        ass()
        if (nextType == TokenType.COLON) {
            // Has returnType
            ass()
            if (nextType != TokenType.IDENTIFIER) throw SyntaxError("Expected return type after colon", currentStartPos, currentEndPos)
            ass()
            returnType = parseIden()
        }

        if (nextType != TokenType.L_BRACE) throw SyntaxError("Expected { after function declaration", currentStartPos, currentEndPos)
        ass()
        val body = parseBracket()

        return FuncNode(name, params, returnType, body, startPos, currentEndPos)
    }

    private fun parseClass(): ClassNode {
        val startPos = currentStartPos
        var params = listOf<ParamNode>()
        var parent: IdenNode? = null

        ass()
        val name = currentToken
        ass()

        // has constructor params
        if (currentType == TokenType.L_PARAN) {
            ass()
            if (currentType == TokenType.IDENTIFIER) {
                params = generateParams()

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
            parent = parseIden()
            ass()
        }

        if (currentType != TokenType.L_BRACE) throw SyntaxError("Expected { after class declaration", currentStartPos, currentEndPos)
        val body = parseBracket()

        return ClassNode(name, params, parent, body, startPos, currentEndPos)
    }

    private fun generateParams(): List<ParamNode> {
        val params = mutableListOf<ParamNode>()
        params += parseParam()

        while (nextType == TokenType.COMMA) {
            ass()
            if (nextType !in listOf(TokenType.IDENTIFIER, TokenType.MULTIPLY, TokenType.POWER)) throw SyntaxError("Expected parameter name after comma", currentStartPos, currentEndPos)
            ass()
            params += parseParam()
        }
        return params
    }

    private fun paramCheck(params: List<ParamNode>) {
        if (params.find { it.variable } != params.findLast { it.variable }) throw SyntaxError("More than 1 variable arguments", currentStartPos, currentEndPos)
        if (params.find { it.kwvariable } != params.findLast { it.kwvariable }) throw SyntaxError("More than 1 variable keyword arguments", currentStartPos, currentEndPos)

        val firstDefault = params.indexOfFirst { it.default != null } .let { if (it == -1) params.size else it }
        if (params.subList(firstDefault, params.size).find { it.default == null } != null) throw SyntaxError("Paramaters with default values must be after those without default values", currentStartPos, currentEndPos)
    }

    private fun parseParam(): ParamNode {
        var variable = false
        var kwvariable = false

        if (currentType == TokenType.MULTIPLY) { variable = true; advance() }
        else if (currentType == TokenType.POWER) { kwvariable = true; advance() }

        if (currentType != TokenType.IDENTIFIER) throw SyntaxError("Illegal character", currentStartPos, currentEndPos)

        val name = parseIden()
        var type: IdenNode? = null
        var default: BaseNode? = null

        if (nextType == TokenType.COLON) {
            ass()
            if (nextType != TokenType.IDENTIFIER) throw SyntaxError("Expected type after :", currentStartPos, currentEndPos)
            ass()
            type = parseIden()
        }

        if (nextType == TokenType.ASSIGN) {
            ass(2)
            default = parseOnce()
        }

        return ParamNode(name, type, default, currentEndPos, variable, kwvariable)
    }

    private fun parseProp(node: BaseNode): PropAccessNode {
        advance()
        advance()
        if (currentType != TokenType.IDENTIFIER) throw SyntaxError("Expected property name after .", currentStartPos, currentEndPos)
        val prop = parseIden()

        return PropAccessNode(node, prop)
    }
}