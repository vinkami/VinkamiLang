package com.vinkami.vinkamilang.language.interpret

import com.vinkami.vinkamilang.language.exception.BaseError
import com.vinkami.vinkamilang.language.exception.NotYourFaultError
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
            is IfNode -> interpretIf(node)
            is LoopNode -> interpretLoop(node)
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

        return try {
            val left = res(interpret(node.left)).obj
            val right = res(interpret(node.right)).obj


            when (node.op.type) {
                TokenType.PLUS -> res(left + right)
                TokenType.MINUS -> res(left - right)
                TokenType.MULTIPLY -> res(left * right)
                TokenType.DIVIDE -> res(left / right)
                TokenType.MODULO -> res(left % right)
                TokenType.POWER -> res(left.power(right))
                else -> throw UnknownNodeError(node)
            }
        }  catch (e: BaseError) {
            res(e)
        } catch (e: UninitializedPropertyAccessException) {
            res
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

    private fun interpretIf(node: IfNode) : InterpretResult {
        val res = InterpretResult(NullObj(node.startPos, node.endPos))

        try {
            val cond = res(interpret(node.condition)).obj
            if (cond.boolVal()) {
                return interpret(node.action)
            }

            for ((elifCondNode, elifActionNode) in node.elif) {
                val elifCond = res(interpret(elifCondNode)).obj
                if (elifCond.boolVal()) {
                    return interpret(elifActionNode)
                }
            }

            if (node.elseAction != null) {
                return interpret(node.elseAction)
            }

        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }

        return res
    }

    private fun interpretLoop(node: LoopNode) : InterpretResult {
        val res = InterpretResult(NullObj(node.startPos, node.endPos))

        try {
            var complete = true
            if (node.loopTokenType == TokenType.WHILE) {
                var cond = res(interpret(node.condition)).obj

                while (cond.boolVal()) {
                    res(interpret(node.mainAction))
                    if (res.interrupt != null) {
                        complete = false
                        res.clearInterrupt()
                        break
                    }
                    cond = res(interpret(node.condition)).obj
                }
            } else throw NotYourFaultError("Unknown loop token type: ${node.loopTokenType}", node.startPos, node.endPos)

            if (complete) {
                res(interpret(node.compAction ?: NullNode(node.startPos, node.endPos)))
            } else {
                res(interpret(node.incompAction ?: NullNode(node.startPos, node.endPos)))
            }
        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }

        return res
    }
}