package com.vinkami.vinkamilang.language.interpret

import com.vinkami.vinkamilang.language.Constant
import com.vinkami.vinkamilang.language.interpret.`object`.builtin.BuiltinFunc
import com.vinkami.vinkamilang.language.exception.*
import com.vinkami.vinkamilang.language.interpret.`object`.*
import com.vinkami.vinkamilang.language.lex.TokenType
import com.vinkami.vinkamilang.language.parse.node.*

class Interpreter(private val node: BaseNode, private val ref: Referables) {
    fun interpret(): InterpretResult = interpret(node, ref)

    private fun interpret(node: BaseNode, ref: Referables): InterpretResult {
        node::class.simpleName ?: return InterpretResult(UnknownNodeError(node))

        return when (node) {
            is NumberNode -> interpretNumber(node)
            is StringNode -> InterpretResult(StringObj(node.value, node.startPos, node.endPos))
            is BinOpNode -> interpretBinOp(node, ref)
            is UnaryOpNode -> interpretUnaryOp(node, ref)
            is IdenNode -> interpretIden(node, ref)
            is AssignNode -> interpretAssign(node, ref)
            is BracketNode -> interpretBracket(node, ref)
            is NullNode -> InterpretResult(NullObj(node.startPos, node.endPos))
            is IfNode -> interpretIf(node, ref)
            is LoopNode -> interpretLoop(node, ref)
            is ProcedralNode -> interpretProcedural(node, ref)
            is FuncNode -> interpretFunc(node, ref)
            else -> InterpretResult(UnknownNodeError(node))
        }
    }

    private fun interpretNumber(node: NumberNode): InterpretResult {
        val valueString = node.value
        return try {
            val value = valueString.toFloat()
            InterpretResult(NumberObj(value, node.startPos, node.endPos))
        } catch (e: NumberFormatException) {
            InterpretResult(SyntaxError("Invalid number: $valueString", node.startPos, node.endPos))
        }
    }

    private fun interpretBinOp(node: BinOpNode, ref: Referables): InterpretResult {
        val res = InterpretResult()

        try {
            if (node.op.type in Constant.difinitiveOp) {
                // Variable assignment
                if (node.left !is IdenNode) return res(SyntaxError("Invalid assignment", node.startPos, node.endPos))
                val name = node.left.name
                val value = interpret(node.right, ref).obj
                val ogValue = ref.get(name) ?: throw NameError("Unknown variable $name", node.left.startPos, node.left.endPos)

                when (node.op.type) {
                    TokenType.ASSIGN -> ref.set(name, value)
                    TokenType.PLUS_ASSIGN -> ref.set(name, ogValue.plus(value))
                    TokenType.MINUS_ASSIGN -> ref.set(name, ogValue.minus(value))
                    TokenType.MULTIPLY_ASSIGN -> ref.set(name, ogValue.times(value))
                    TokenType.DIVIDE_ASSIGN -> ref.set(name, ogValue.divide(value))
                    TokenType.MODULO_ASSIGN -> ref.set(name, ogValue.mod(value))
                    TokenType.POWER_ASSIGN -> ref.set(name, ogValue.power(value))
                    else -> throw NotYourFaultError("Invalid assignment operator ${node.op.type}", node.op.startPos, node.op.endPos)  // No other TT are allowed from parser
                }
                res(NullObj(node.startPos, node.endPos))

            } else {
                // Normal caluclation
                val left = res(interpret(node.left, ref)).obj
                val right = res(interpret(node.right, ref)).obj

                when (node.op.type) {
                    TokenType.PLUS -> res(left.plus(right))
                    TokenType.MINUS -> res(left.minus(right))
                    TokenType.MULTIPLY -> res(left.times(right))
                    TokenType.DIVIDE -> res(left.divide(right))
                    TokenType.MODULO -> res(left.mod(right))
                    TokenType.POWER -> res(left.power(right))

                    TokenType.EQUAL -> res(left.equal(right))
                    TokenType.NOT_EQUAL -> res(left.notEqual(right))
                    TokenType.LESS_EQUAL -> res(left.lessEqual(right))
                    TokenType.GREATER_EQUAL -> res(left.greaterEqual(right))
                    TokenType.LESS -> res(left.less(right))
                    TokenType.GREATER -> res(left.greater(right))

                    else -> throw UnknownNodeError(node)
                }
            }
        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }

        return res
    }

    private fun interpretUnaryOp(node: UnaryOpNode, ref: Referables): InterpretResult {
        val res = InterpretResult()

        val innerNode = res(interpret(node.innerNode, ref)).also{res.hasError && return res}.obj

        return when (node.op.type) {
            TokenType.PLUS -> res(innerNode.unaryPlus())
            TokenType.MINUS -> res(innerNode.unaryMinus())
            else -> res(UnknownNodeError(node))
        }
    }

    private fun interpretIden(node: IdenNode, ref: Referables): InterpretResult {
        val res = InterpretResult()

        try {
            val obj = ref.get(node.name) ?: throw NameError("Undefined name \"${node.name}\"", node.startPos, node.endPos)
            if (!node.withCall) res(obj)
            else {
                when (obj) {
                    is FuncObj -> {
                        val funcRef = ref.bornChild()
                        // set given params
                        for (i in 0 until node.args.size) {
                            val arg = res(interpret(node.args[i], ref)).obj
                            funcRef.set(obj.node.params[i].name, arg)
                        }
                        for ((keyToken, valueNode) in node.kwargs) {
                            val kwval = res(interpret(valueNode, ref)).obj
                            funcRef.set(keyToken.value, kwval)
                        }
                        // check for missing
                        for (param in obj.node.params) {
                            if (funcRef.get(param.name) == null) {
                                if (param.default != null) {
                                    val defaultValue = res(interpret(param.default, ref)).obj
                                    funcRef.set(param.name, defaultValue)
                                } else {
                                    throw TypeError("Missing argument \"${param.name}\"", node.startPos, node.endPos)
                                }
                            }
                        }
                        res(res(interpret(obj.node.body, funcRef)).obj)
                    }

                    is BuiltinFunc -> {
                        val funcRef = ref.bornChild()
                        // set given params
                        for (i in 0 until node.args.size) {
                            val arg = res(interpret(node.args[i], ref)).obj
                            funcRef.set(obj.parameters[i].name, arg)
                        }
                        for ((keyToken, valueNode) in node.kwargs) {
                            val kwval = res(interpret(valueNode, ref)).obj
                            funcRef.set(keyToken.value, kwval)
                        }
                        // check for missing
                        for (param in obj.parameters) {
                            if (funcRef.get(param.name) == null) {
                                if (param.default != null) {
                                    val defaultValue = res(interpret(param.default, ref)).obj
                                    funcRef.set(param.name, defaultValue)
                                } else {
                                    throw TypeError("Missing argument \"${param.name}\"", node.startPos, node.endPos)
                                }
                            }
                        }
                        res(obj(funcRef))
                    }

                    else -> return res(TypeError("${obj::class.simpleName} is not callable", node.startPos, node.endPos))
                }
            }
        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }

        return res
    }

    private fun interpretAssign(node: AssignNode, ref: Referables): InterpretResult {
        val res = InterpretResult()

        return try {
            val value = res(interpret(node.value, ref)).obj

            ref.set(node.iden.name, value)
            res(NullObj(node.startPos, node.endPos))
        } catch (e: BaseError) { res(e) } catch (e: UninitializedPropertyAccessException) { res }
    }

    private fun interpretBracket(node: BracketNode, ref: Referables): InterpretResult {
        return interpret(node.innerNode, ref)
    }

    private fun interpretIf(node: IfNode, ref: Referables): InterpretResult {
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

    // TODO: Add for loop
    private fun interpretLoop(node: LoopNode, globalRef: Referables): InterpretResult {
        val res = InterpretResult()
        val ref = globalRef.bornChild()
        var finalObj: BaseObject = NullObj(node.startPos, node.endPos)

        try {
            var complete = true
            if (node.loopTokenType == TokenType.WHILE) {
                var cond = res(interpret(node.condition, ref)).obj

                while (cond.boolVal()) {
                    val innerRes = res(interpret(node.mainAction, ref))
                    if (innerRes.hasObject) finalObj = innerRes.obj
                    if (res.interrupt != null) {
                        complete = false
                        res.clearInterrupt()
                        break
                    }
                    cond = res(interpret(node.condition, ref)).obj
                }

            } else throw NotYourFaultError("Unknown loop token type: ${node.loopTokenType}", node.startPos, node.endPos)

            if (complete) {
                val compRes = res(interpret(node.compAction ?: NullNode(node.startPos, node.endPos), ref))
                if (compRes.hasObject) finalObj = compRes.obj
            } else {
                val incompRes = res(interpret(node.incompAction ?: NullNode(node.startPos, node.endPos), ref))
                if (incompRes.hasObject) finalObj = incompRes.obj
            }
        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }

        return res(finalObj)
    }

    private fun interpretProcedural(node: ProcedralNode, ref: Referables): InterpretResult {
        val res = InterpretResult()
        val localRef = ref.bornChild()
        var finalObj: BaseObject = NullObj(node.startPos, node.endPos)

        try {
            for (procedure in node.procedures) {
                finalObj = res(interpret(procedure, localRef)).obj  // throw UPAE if res has error

                if (res.interrupt != null) {
                    res.clearInterrupt()
                    break
                }
            }
        } catch (e: BaseError) { return res(e) } catch (e: UninitializedPropertyAccessException) { return res }

        return res(finalObj)
    }

    private fun interpretFunc(node: FuncNode, ref: Referables): InterpretResult {
        ref.set(node.name.value, FuncObj(node))
        return InterpretResult(NullObj(node.startPos, node.endPos))
    }
}