package com.vinkami.vinkamilang.language.interpret

import com.vinkami.vinkamilang.language.exception.SyntaxError
import com.vinkami.vinkamilang.language.exception.UnknownNodeError
import com.vinkami.vinkamilang.language.interpret.`object`.NumberObj
import com.vinkami.vinkamilang.language.lex.TokenType
import com.vinkami.vinkamilang.language.parse.node.BaseNode
import com.vinkami.vinkamilang.language.parse.node.BinOpNode
import com.vinkami.vinkamilang.language.parse.node.BracketNode
import com.vinkami.vinkamilang.language.parse.node.NumberNode

class Interpreter(val node: BaseNode) {
    fun interpret(): InterpretResult = interpret(node)

    private fun interpret(node: BaseNode): InterpretResult {
        node::class.simpleName ?: return InterpretResult(UnknownNodeError(node))

        return when (node) {
            is NumberNode -> interpretNumber(node)
            is BinOpNode -> interpretBinOp(node)
            is BracketNode -> interpretBracket(node)
            else -> InterpretResult(UnknownNodeError(node))
        }
    }

    private fun interpretNumber(node: NumberNode): InterpretResult {
        val valueString = node.tok.value
        return try {
            val value = valueString.toFloat()
            InterpretResult(NumberObj(value))
        } catch (e: NumberFormatException) {
            InterpretResult(SyntaxError("Invalid number: $valueString", node.startPos, node.endPos))
        }
    }

    private fun interpretBinOp(node: BinOpNode): InterpretResult {
        val res = InterpretResult()

        val left = res(interpret(node.left)).obj as NumberObj
        val right = res(interpret(node.right)).obj as NumberObj
        res.hasError && return res

        return when (node.op.type) {
            TokenType.PLUS -> res(NumberObj(left.value + right.value))
            TokenType.MINUS -> res(NumberObj(left.value - right.value))
            TokenType.MULTIPLY -> res(NumberObj(left.value * right.value))
            TokenType.DIVIDE -> res(NumberObj(left.value / right.value))
            else -> res(UnknownNodeError(node))
        }
    }

    private fun interpretBracket(node: BracketNode): InterpretResult {
        return interpret(node.node)
    }
}