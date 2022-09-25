package com.vinkami.vinkamilang.language.interpret

import com.vinkami.vinkamilang.language.exception.SyntaxError
import com.vinkami.vinkamilang.language.exception.UnknownNodeError
import com.vinkami.vinkamilang.language.interpret.`object`.NullObj
import com.vinkami.vinkamilang.language.interpret.`object`.NumberObj
import com.vinkami.vinkamilang.language.lex.TokenType
import com.vinkami.vinkamilang.language.parse.node.*

class Interpreter(val node: BaseNode) {
    fun interpret(): InterpretResult = interpret(node)

    private fun interpret(node: BaseNode): InterpretResult {
        node::class.simpleName ?: return InterpretResult(UnknownNodeError(node))

        return when (node) {
            is NumberNode -> interpretNumber(node)
            is BinOpNode -> interpretBinOp(node)
            is UnaryOpNode -> interpretUnaryOp(node)
            is BracketNode -> interpretBracket(node)
            is NullNode -> InterpretResult(NullObj(node.startPos, node.endPos))
            else -> InterpretResult(UnknownNodeError(node))
        }
    }

    private fun interpretNumber(node: NumberNode): InterpretResult {
        val valueString = node.tok.value
        return try {
            val value = valueString.toFloat()
            InterpretResult(NumberObj(value, node.startPos, node.endPos))
        } catch (e: NumberFormatException) {
            InterpretResult(SyntaxError("Invalid number: $valueString", node.startPos, node.endPos))
        }
    }

    private fun interpretBinOp(node: BinOpNode): InterpretResult {
        val res = InterpretResult()

        val left = res(interpret(node.left)).also{res.hasError && return res}.obj
        val right = res(interpret(node.right)).also{res.hasError && return res}.obj


        return when (node.op.type) {
            TokenType.PLUS -> res(left + right)
            TokenType.MINUS -> res(left - right)
            TokenType.MULTIPLY -> res(left * right)
            TokenType.DIVIDE -> res(left / right)
            TokenType.MODULO -> res(left % right)
            TokenType.POWER -> res(left.power(right))
            else -> res(UnknownNodeError(node))
        }
    }

    private fun interpretUnaryOp(node: UnaryOpNode): InterpretResult {
        val res = InterpretResult()

        val innerNode = res(interpret(node.innerNode)).also{res.hasError && return res}.obj

        return when (node.op.type) {
            TokenType.PLUS -> res(+innerNode)
            TokenType.MINUS -> res(-innerNode)
            else -> res(UnknownNodeError(node))
        }
    }

    private fun interpretBracket(node: BracketNode): InterpretResult {
        return interpret(node.innerNode)
    }
}