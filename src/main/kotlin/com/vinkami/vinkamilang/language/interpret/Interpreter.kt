package com.vinkami.vinkamilang.language.interpret

import com.vinkami.vinkamilang.language.exception.*
import com.vinkami.vinkamilang.language.interpret.`object`.*
import com.vinkami.vinkamilang.language.lex.TokenType
import com.vinkami.vinkamilang.language.parse.node.*

class Interpreter(val node: BaseNode) {
    fun interpret(): InterpretResult = interpret(node, Referables())

    private fun interpret(node: BaseNode, ref: Referables): InterpretResult {
        node::class.simpleName ?: return InterpretResult(UnknownNodeError(node))

        return when (node) {
            is NumberNode -> interpretNumber(node)
            is BinOpNode -> interpretBinOp(node, ref)
            is UnaryOpNode -> interpretUnaryOp(node, ref)
            is VarNode -> interpretVar(node, ref)
            is BracketNode -> interpretBracket(node, ref)
            is NullNode -> InterpretResult(NullObj(node.startPos, node.endPos))
            is IfNode -> interpretIf(node, ref)
            is LoopNode -> interpretLoop(node, ref)
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

    private fun interpretBinOp(node: BinOpNode, ref: Referables): InterpretResult {
        val res = InterpretResult()

        return try {
            val left = res(interpret(node.left, ref)).obj
            val right = res(interpret(node.right, ref)).obj


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

    private fun interpretUnaryOp(node: UnaryOpNode, ref: Referables): InterpretResult {
        val res = InterpretResult()

        val innerNode = res(interpret(node.innerNode, ref)).also{res.hasError && return res}.obj

        return when (node.op.type) {
            TokenType.PLUS -> res(+innerNode)
            TokenType.MINUS -> res(-innerNode)
            else -> res(UnknownNodeError(node))
        }
    }

    // TODO: untested
    private fun interpretVar(node: VarNode, ref: Referables): InterpretResult {
        val name = node.name
        val value = ref.locate(name) ?: return InterpretResult(NameError("Variable '$name' is not defined", node.startPos, node.endPos))
        return InterpretResult(value)
    }

    private fun interpretBracket(node: BracketNode, ref: Referables): InterpretResult {
        return interpret(node.innerNode, ref)
    }

    private fun interpretIf(node: IfNode, ref: Referables) : InterpretResult {
        val res = InterpretResult(NullObj(node.startPos, node.endPos))
        val localRef = ref.bornChild()

        try {
            val cond = res(interpret(node.condition, localRef)).obj
            if (cond.boolVal()) {
                return interpret(node.action, localRef)
            }

            for ((elifCondNode, elifActionNode) in node.elif) {
                val elifCond = res(interpret(elifCondNode, localRef)).obj
                if (elifCond.boolVal()) {
                    return interpret(elifActionNode, localRef)
                }
            }

            if (node.elseAction != null) {
                return interpret(node.elseAction, localRef)
            }

        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }

        return res
    }

    // TODO: check it after variables are implemented
    private fun interpretLoop(node: LoopNode, ref: Referables) : InterpretResult {
        val res = InterpretResult()
        val localRef = ref.bornChild()

        try {
            var complete = true
            if (node.loopTokenType == TokenType.WHILE) {
                var cond = res(interpret(node.condition, localRef)).obj

                while (cond.boolVal()) {
                    res(interpret(node.mainAction, localRef))
                    if (res.interrupt != null) {
                        complete = false
                        res.clearInterrupt()
                        break
                    }
                    cond = res(interpret(node.condition, localRef)).obj
                }
            } else throw NotYourFaultError("Unknown loop token type: ${node.loopTokenType}", node.startPos, node.endPos)

            if (complete) {
                res(interpret(node.compAction ?: NullNode(node.startPos, node.endPos), localRef))
            } else {
                res(interpret(node.incompAction ?: NullNode(node.startPos, node.endPos), localRef))
            }
        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }

        return res
    }
}