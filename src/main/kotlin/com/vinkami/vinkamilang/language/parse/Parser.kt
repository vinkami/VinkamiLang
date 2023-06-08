package com.vinkami.vinkamilang.language.parse

import com.vinkami.vinkamilang.language.Constant
import com.vinkami.vinkamilang.language.exception.*
import com.vinkami.vinkamilang.language.lex.Position
import com.vinkami.vinkamilang.language.lex.Token
import com.vinkami.vinkamilang.language.lex.TokenType
import com.vinkami.vinkamilang.language.lex.TokenType.*
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
        get() = tokens.subList(pos + 1, tokens.size).firstOrNull { it.type !in listOf(SPACE, LINEBREAK) } ?: tokens.last()
    private val nextType: TokenType
        get() = nextNonSpaceToken.type

    init {advance()}

    private fun advance() {
        if (pos == tokens.size - 1) throw SyntaxError("Unexpected end of file", currentStartPos, currentEndPos)
        pos++
    }

    private fun skipSpace() {
        while (listOf(SPACE, LINEBREAK).contains(currentType)) advance()
    }

    /**
     * Advance, Skip Space
     */
    private tailrec fun ass(n: Int = 1) {
        advance()
        skipSpace()
        return if (n == 1) Unit else ass(n-1)
    }

    fun parse(): BaseNode {
        val procedures = mutableListOf<BaseNode>()
        val startPos = currentStartPos

        skipSpace()
        while (true) {
            val procedure = parseOnce()
            if (procedure is ArgumentsNode) throw SyntaxError("Nothing to be called", procedure.startPos, procedure.endPos)
            else procedures += procedure
            if (currentType == EOF) break
            ass()
        }
        val endPos = currentEndPos

        if (procedures.size == 1 || procedures.size == 2) return procedures[0]  // 1: Only NullNode from EOF; 2: Only one procedure
        return ProcedureNode(procedures, startPos, endPos)
    }

    private fun parseOnce(): BaseNode {
        var currentProcedure = when (currentType) {
            NUMBER, IDENTIFIER, TRUE, FALSE -> parseBinOp(0)
            STRING -> StringNode(currentToken)
            PLUS, MINUS, NOT -> parseUnaryOp()
            VAR, VAL -> parseAssign()
            in Constant.bracket.keys -> parseBracket()
            IF -> parseIf()
            WHILE, FOR -> parseLoop()
            BREAK -> parseInterrupt()
            FUNC -> parseFuncDef()
            CLASS -> parseClass()
            EOF -> NullNode(currentToken)
            else -> throw SyntaxError("Unexpected token $currentType", currentStartPos, currentEndPos)
        }

        while (true) {
            if (pos == tokens.size - 1) break
            if (tokens[pos+1].type == DOT) { // gets property
                advance()
                currentProcedure = parseProp(currentProcedure)
            }
            else if (tokens[pos+1].type == L_PARAN) {  // makes call
                val cpos = pos
                advance()
                val arguments = parseBracket()
                if (arguments !is ArgumentsNode) {
                    pos = cpos
                    break
                }
                currentProcedure.call = arguments
            }
            else break
        }

        return currentProcedure
    }

//    private fun parseExprOnce(): BaseNode { // Only expressions, not statements
//        return when (currentType) {
//            NUMBER -> NumberNode(currentToken)
//            IDENTIFIER -> parseIden()
//            STRING -> StringNode(currentToken)
//            PLUS, MINUS, NOT -> parseUnaryOp()
//            in Constant.bracket.keys -> parseBracket()
//            else -> throw SyntaxError("Unexpected token $currentType", currentStartPos, currentEndPos)
//        }
//    }

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
            L_PARAN -> parseBracket()
            NUMBER -> NumberNode(currentToken)
            IDENTIFIER -> parseIden()
            TRUE, FALSE -> BoolNode(currentToken)
            PLUS, MINUS, NOT -> parseUnaryOp()
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
            if (op.type !in Constant.binaryOps) break

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
        val inner = parseOnce()
//        val inner = parseExprOnce()
        return processBinOp(0, UnaryOpNode(op, inner))  // unary node may be a part of math expression
    }

    private fun parseIden(): IdenNode = IdenNode(currentToken)

    private fun parseAssign(): AssignNode {
        val startPos = currentStartPos

        val mutable = currentType == VAR
        ass()

        if (currentType != IDENTIFIER) throw SyntaxError("Expected identifier after var", currentStartPos, currentEndPos)
        val iden = parseIden()
        ass()

        if (currentType != ASSIGN) throw SyntaxError("Expected assignment operator after identifier", currentStartPos, currentEndPos)
        ass()

        if (currentType in listOf(EOF, LINEBREAK)) throw SyntaxError("Unexpected end of line", currentStartPos, currentEndPos)
        val value = parseOnce()

        return AssignNode(iden, value, mutable, startPos)
    }

    private fun parseBracket(): BaseNode {
        val bracketTypeL = currentType
        if (bracketTypeL !in Constant.bracket.keys) throw NotYourFaultError("Illegal bracket type $bracketTypeL", currentStartPos, currentEndPos)
        val bracketTypeR = Constant.bracket[bracketTypeL]

        var paranCount = 1
        val start = pos
        val startToken = currentToken
        val startPos = currentStartPos

        while (paranCount > 0) {  // Find the matching closing bracket in terms of number
            advance()
            val tt = currentType
            if (tt == EOF) throw SyntaxError("Script ended when expecting a $bracketTypeR", currentStartPos, currentEndPos)
            else if (Constant.bracket.keys.contains(tt)) paranCount++
            else if (Constant.bracket.values.contains(tt)) paranCount--
        }

        // Confirm the "matching" bracket is the same type
        if (currentType != bracketTypeR) throw SyntaxError("Expected $bracketTypeR, got $currentType", currentStartPos, currentEndPos)

        val endToken = currentToken
        val eof = Token(EOF, "EOF", endToken.startPos, endToken.endPos)
        val innerTokens = tokens.subList(start + 1, pos) + eof

        return when (bracketTypeL) {
            L_PARAN -> {
                if (start == 0 || tokens[start - 1].type in listOf(PLUS, MINUS, SPACE, LINEBREAK)) {  // parse as if it's a part of math expr
                    val innerResult = Parser(innerTokens).parse()
                    val node = BracketNode(startToken, innerResult, endToken)
                    processBinOp(0, node)  // Try to continue parsing the bracket as a binop, return itself if not anyway
                } else {  // parse as a list of arguments
                    val (args, kwargs) = Parser(innerTokens).generateArguments()
                    ArgumentsNode(args, kwargs, startToken.startPos, currentEndPos)
                }
            }

            L_BRAC -> {  // lua table
                val (args, kwargs) = Parser(innerTokens).generateArguments()
                if (args.isNotEmpty() && kwargs.isNotEmpty()) throw SyntaxError("Table type unsure", startPos, currentEndPos)

                if (kwargs.isEmpty()) ListNode(args, startToken.startPos, currentEndPos)  // both empty -> empty list
                else DictNode(kwargs, startToken.startPos, currentEndPos)
            }

            L_BRACE -> {
                val innerResult = Parser(innerTokens).parse()
                BracketNode(startToken, innerResult, endToken)
            }

            else -> throw NotYourFaultError("parseBracket() check got bypassed with illegal bracket type $bracketTypeL", currentStartPos, currentEndPos)
        }
    }

    private fun generateArguments(): Pair<List<BaseNode>, Map<Token, BaseNode>> {
        val args = mutableListOf<BaseNode>()
        val kwargs = mutableMapOf<Token, BaseNode>()

        skipSpace()
        var argsEnd = false
        var paramsEnd = currentType == EOF

        while (!paramsEnd) {
            if (!argsEnd) {
                if (nextType == ASSIGN) {  // treat this and all future params as kwargs
                    argsEnd = true
                    val name = currentToken
                    ass(2)
                    kwargs[name] = parseOnce()
                } else {
                    args += parseOnce()
                }

            } else {  // kwargs
                val name = currentToken
                if (nextType != ASSIGN) throw SyntaxError("No assign symbol in between", currentStartPos, currentEndPos)
                ass(2)
                kwargs[name] = parseOnce()
            }

            if (nextType == EOF) paramsEnd = true
            else if (nextType != COMMA) throw SyntaxError("No comma seperating arguments", currentStartPos, currentEndPos)
            else ass(2)
        }

        return args to kwargs
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
        while (currentType == ELIF) {
            ass()
            val cond = parseBracket()
            ass()
            val action = parseBracket()

            endPos = currentEndPos
            elif[cond] = action
            ass()
        }

        var elseAction: BaseNode? = null
        if (currentType == ELSE) {
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
                COMPLETE -> compAction = parseBracket()
                INCOMPLETE -> incompAction = parseBracket()
                else -> throw NotYourFaultError("Illegal loop complete type $tt", currentStartPos, currentEndPos)
            }
            endPos = currentEndPos
        }

        return LoopNode(loopTT, condition, mainAction, compAction, incompAction, startPos, endPos)
    }

    private fun parseInterrupt(): InterruptNode {
        val startPos = currentStartPos
        val type = currentType
        advance()
        while (currentType == SPACE) advance()

        val node = if (currentType == LINEBREAK) NullNode(currentToken) else parseOnce()
        return InterruptNode(node, type, startPos)
    }

    private fun parseFuncDef(): FuncNode {
        val startPos = currentStartPos
        var params = listOf<ParamNode>()
        var returnType: IdenNode? = null

        ass()

        val name = currentToken
        ass()

        if (currentType != L_PARAN) throw SyntaxError("Expected ( after function name", currentStartPos, currentEndPos)

        if (nextType != R_PARAN) {
            // Has params
            ass()
            params = generateParams()
            paramCheck(params)
        }

        ass()
        if (nextType == COLON) {
            // Has returnType
            ass()
            if (nextType != IDENTIFIER) throw SyntaxError("Expected return type after colon", currentStartPos, currentEndPos)
            ass()
            returnType = parseIden()
        }

        if (nextType != L_BRACE) throw SyntaxError("Expected { after function declaration", currentStartPos, currentEndPos)
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
        if (currentType == L_PARAN) {
            ass()
            if (currentType == IDENTIFIER) {
                params = generateParams()

                if (nextType != R_PARAN) throw SyntaxError("Unclosed bracket", currentStartPos, currentEndPos)
                ass(2)
            } else if (currentType != R_PARAN) throw SyntaxError("Unclosed bracket", currentStartPos, currentEndPos)
            else ass()  // skip useless R_PARAN

            paramCheck(params)
        }

        // has inheritance
        if (currentType == COLON) {
            ass()
            if (currentType != IDENTIFIER) throw SyntaxError("Expect class name after colon", currentStartPos, currentEndPos)
            parent = parseIden()
            ass()
        }

        if (currentType != L_BRACE) throw SyntaxError("Expected { after class declaration", currentStartPos, currentEndPos)
        val body = parseBracket()

        return ClassNode(name, params, parent, body, startPos, currentEndPos)
    }

    private fun generateParams(): List<ParamNode> {
        val params = mutableListOf<ParamNode>()
        params += parseParam()

        while (nextType == COMMA) {
            ass()
            if (nextType !in listOf(IDENTIFIER, MULTIPLY, POWER)) throw SyntaxError("Expected parameter name after comma", currentStartPos, currentEndPos)
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

        if (currentType == MULTIPLY) { variable = true; advance() }
        else if (currentType == POWER) { kwvariable = true; advance() }

        if (currentType != IDENTIFIER) throw SyntaxError("Illegal character", currentStartPos, currentEndPos)

        val name = parseIden()
        var type: IdenNode? = null
        var default: BaseNode? = null

        if (nextType == COLON) {
            ass()
            if (nextType != IDENTIFIER) throw SyntaxError("Expected type after :", currentStartPos, currentEndPos)
            ass()
            type = parseIden()
        }

        if (nextType == ASSIGN) {
            ass(2)
            default = parseOnce()
        }

        return ParamNode(name, type, default, currentEndPos, variable, kwvariable)
    }

    private fun parseProp(node: BaseNode): PropAccessNode {
        if (currentType != DOT) throw NotYourFaultError("Not accessing property with a .", currentStartPos, currentEndPos)
        advance()
        if (currentType != IDENTIFIER) throw SyntaxError("Expected property name after .", currentStartPos, currentEndPos)
        val prop = parseIden()

        return PropAccessNode(node, prop)
    }
}