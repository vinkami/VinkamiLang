package com.vinkami.vinkamilang.language.interpret

import com.vinkami.vinkamilang.language.Constant
import com.vinkami.vinkamilang.language.interpret.`object`.builtin.BuiltinFunc
import com.vinkami.vinkamilang.language.exception.*
import com.vinkami.vinkamilang.language.interpret.`object`.*
import com.vinkami.vinkamilang.language.interpret.`object`.builtin.Parameter
import com.vinkami.vinkamilang.language.lex.Position
import com.vinkami.vinkamilang.language.lex.Token
import com.vinkami.vinkamilang.language.lex.TokenType
import com.vinkami.vinkamilang.language.parse.node.*

class Interpreter(private val globalNode: BaseNode, private val globalRef: Referables) {
    fun interpret(): InterpretResult {
        return try {
            interpret(globalNode, globalRef)
        } catch (e: BaseError) {
            InterpretResult(e)
        }
    }

    private fun interpret(node: BaseNode, ref: Referables): InterpretResult {
        node::class.simpleName ?: return InterpretResult(UnknownNodeError(node))

        return InterpretResult(when (node) {
            is NumberNode -> interpretNumber(node)
            is StringNode -> StringObj(node.value, node.startPos, node.endPos)
            is BinOpNode -> interpretBinOp(node, ref)
            is UnaryOpNode -> interpretUnaryOp(node, ref)
            is IdenNode -> interpretIden(node, ref)
            is AssignNode -> interpretAssign(node, ref)
            is BracketNode -> interpretBracket(node, ref)
            is NullNode -> NullObj(node.startPos, node.endPos)
            is IfNode -> interpretIf(node, ref)
            is LoopNode -> interpretLoop(node, ref)
            is ProcedureNode -> interpretProcedural(node, ref)
            is FuncNode -> interpretFuncCreation(node, ref)
            is ClassNode -> interpretClassCreation(node, ref)
            is PropAccessNode -> interpretPropAccess(node, ref)
            else -> throw UnknownNodeError(node)
        })
    }

    private fun interpretNumber(node: NumberNode): BaseObject {
        val valueString = node.value
        try {
            val value = valueString.toFloat()
            return NumberObj(value, node.startPos, node.endPos)
        } catch (e: NumberFormatException) {
            throw SyntaxError("Invalid number: $valueString", node.startPos, node.endPos)
        }
    }

    private fun interpretBinOp(node: BinOpNode, ref: Referables): BaseObject {
        if (node.op.type in Constant.difinitiveOp) {
            // Variable assignment
            if (node.left !is IdenNode) throw SyntaxError("Invalid assignment", node.startPos, node.endPos)
            val name = node.left.name
            val value = interpret(node.right, ref).obj
            val ogValue =
                ref.get(name) ?: throw NameError("Unknown variable $name", node.left.startPos, node.left.endPos)

            when (node.op.type) {
                TokenType.ASSIGN -> ref.set(name, value)
                TokenType.PLUS_ASSIGN -> ref.set(name, ogValue.plus(value))
                TokenType.MINUS_ASSIGN -> ref.set(name, ogValue.minus(value))
                TokenType.MULTIPLY_ASSIGN -> ref.set(name, ogValue.times(value))
                TokenType.DIVIDE_ASSIGN -> ref.set(name, ogValue.divide(value))
                TokenType.MODULO_ASSIGN -> ref.set(name, ogValue.mod(value))
                TokenType.POWER_ASSIGN -> ref.set(name, ogValue.power(value))
                else -> throw NotYourFaultError(
                    "Invalid assignment operator ${node.op.type}",
                    node.op.startPos,
                    node.op.endPos
                )  // No other TT are allowed from parser
            }
            return NullObj(node.startPos, node.endPos)

        } else {
            // Normal caluclation
            val left = interpret(node.left, ref).obj
            val right = interpret(node.right, ref).obj

            return when (node.op.type) {
                TokenType.PLUS -> left.plus(right)
                TokenType.MINUS -> left.minus(right)
                TokenType.MULTIPLY -> left.times(right)
                TokenType.DIVIDE -> left.divide(right)
                TokenType.MODULO -> left.mod(right)
                TokenType.POWER -> left.power(right)

                TokenType.EQUAL -> left.equal(right)
                TokenType.NOT_EQUAL -> left.notEqual(right)
                TokenType.LESS_EQUAL -> left.lessEqual(right)
                TokenType.GREATER_EQUAL -> left.greaterEqual(right)
                TokenType.LESS -> left.less(right)
                TokenType.GREATER -> left.greater(right)

                else -> throw UnknownNodeError(node)
            }
        }
    }

    private fun interpretUnaryOp(node: UnaryOpNode, ref: Referables): BaseObject {
        val innerNode = interpret(node.innerNode, ref).obj

        return when (node.op.type) {
            TokenType.PLUS -> innerNode.unaryPlus()
            TokenType.MINUS -> innerNode.unaryMinus()
            else -> throw UnknownNodeError(node)
        }
    }

    private fun interpretIden(node: IdenNode, ref: Referables): BaseObject {
        val obj = ref.get(node.name) ?: throw NameError("Undefined name \"${node.name}\"", node.startPos, node.endPos)
        return if (node.withCall) makeCall(obj, node.args, node.kwargs, ref, node.startPos, node.endPos)
        else obj
    }

    private fun makeCall(obj: BaseObject, args: List<BaseNode>, kwargs: Map<Token, BaseNode>, ref: Referables, startPos: Position, endPos: Position): BaseObject {
        val localRef = ref.bornChild()

        return when (obj) {
            is FuncObj -> interpretFunc(obj.node, args, kwargs, localRef, startPos, endPos)
            is BuiltinFunc -> interpretBultinFunc(obj, args, kwargs, localRef, startPos, endPos)
            is ClassObj -> interpretClass(obj.node, args, kwargs, localRef, startPos, endPos)
            else -> throw TypeError("${obj::class.simpleName} is not callable", startPos, endPos)
        }
    }

    private fun interpretAssign(node: AssignNode, ref: Referables): BaseObject {
        val value = interpret(node.value, ref).obj

        ref.set(node.iden.name, value)
        return NullObj(node.startPos, node.endPos)
    }

    private fun interpretBracket(node: BracketNode, ref: Referables): BaseObject {
        return interpret(node.innerNode, ref).obj
    }

    private fun interpretIf(node: IfNode, ref: Referables): BaseObject {
        val localRef = ref.bornChild()

        val cond = interpret(node.condition, localRef).obj
        if (cond.boolVal()) {
            return interpret(node.action, localRef).obj
        }

        for ((elifCondNode, elifActionNode) in node.elif) {
            val elifCond = interpret(elifCondNode, localRef).obj
            if (elifCond.boolVal()) {
                return interpret(elifActionNode, localRef).obj
            }
        }

        if (node.elseAction != null) {
            return interpret(node.elseAction, localRef).obj
        }

        return NullObj(node.startPos, node.endPos)
    }

    // TODO: Add for loop
    private fun interpretLoop(node: LoopNode, globalRef: Referables): BaseObject {
        val ref = globalRef.bornChild()
        var finalObj: BaseObject = NullObj(node.startPos, node.endPos)

        var complete = true
        if (node.loopTokenType == TokenType.WHILE) {
            var cond = interpret(node.condition, ref).obj

            while (cond.boolVal()) {
                val res = interpret(node.mainAction, ref)
                if (res.hasObject) finalObj = res.obj
                if (res.interrupt != null) {
                    complete = false
                    break
                }
                cond = interpret(node.condition, ref).obj
            }

        } else throw NotYourFaultError("Unknown loop token type: ${node.loopTokenType}", node.startPos, node.endPos)

        if (complete) {
            val compRes = interpret(node.compAction ?: NullNode(node.startPos, node.endPos), ref)
            if (compRes.hasObject) finalObj = compRes.obj
        } else {
            val incompRes = interpret(node.incompAction ?: NullNode(node.startPos, node.endPos), ref)
            if (incompRes.hasObject) finalObj = incompRes.obj
        }

        return finalObj
    }

    private fun interpretProcedural(node: ProcedureNode, ref: Referables): BaseObject {
        for (procedure in node.procedures) {
            val res = interpret(procedure, ref)

            if (res.interrupt != null) {
                res.clearInterrupt()
                break
            }
        }
        return NullObj(node.startPos, node.endPos)
    }

    private fun interpretFuncCreation(node: FuncNode, ref: Referables): BaseObject {
        ref.set(node.name.value, FuncObj(node))
        return NullObj(node.startPos, node.endPos)
    }

    private fun interpretFunc(node: FuncNode, args: List<BaseNode>, kwargs: Map<Token, BaseNode>, ref: Referables, startPos: Position, endPos: Position): BaseObject {
        val params = node.params.map { convertParamNode(it, ref) }
        setParams(params, args, kwargs, ref, startPos, endPos)
        return interpret(node.body, ref).obj
    }

    private fun interpretBultinFunc(obj: BuiltinFunc, args: List<BaseNode>, kwargs: Map<Token, BaseNode>, ref: Referables, startPos: Position, endPos: Position): BaseObject {  // BuiltinFunc doesn't have positional information
        setParams(obj.parameters, args, kwargs, ref, startPos, endPos)
        return obj(ref)
    }

    private fun interpretClassCreation(node: ClassNode, ref: Referables): BaseObject {
        ref.set(node.name.value, ClassObj(node))
        return NullObj(node.startPos, node.endPos)
    }

    private fun interpretClass(node: ClassNode, args: List<BaseNode>, kwargs: Map<Token, BaseNode>, ref: Referables, startPos: Position, endPos: Position): BaseObject {
        var refnew: Referables
        setParams(node.initParams.map { convertParamNode(it, ref) }, args, kwargs, ref, startPos, endPos)
            .let { refnew = it }
        // set class methods, constants, etc. and does the init work
        // TODO: inheritance
        interpret(node.body, refnew)
        return CustomObj(node.name.value, ref, node.startPos, node.endPos)
    }

    private fun convertParamNode(node: ParamNode, ref: Referables): Parameter {
        val type = node.type?.name
        val default = node.default?.let { interpret(node.default, ref).obj }
        return Parameter(node.name, type, default, node.variable, node.kwvariable)
    }

    private fun setParams(params: List<Parameter>, args: List<BaseNode>, kwargs: Map<Token, BaseNode>, ref: Referables, startPos: Position, endPos: Position): Referables {
        val varArg = params.find { it.variable }
        val varKwarg = params.find { it.kwvariable }

        // normal arguments
        val modifier = (if (varArg != null) 1 else 0) + (if (varKwarg != null) 1 else 0)
        val argParamNumber = params.count { it.default == null } - modifier
        if (args.size < argParamNumber) throw TypeError("Not enough arguments", startPos, endPos)
        if (varArg != null) {
            val varArgsIndex = params.indexOf(varArg)

            // first part of arguments
            args.subList(0, varArgsIndex).forEachIndexed { i, it ->  ref.set(params[i].name, interpret(it, ref).obj) }

            // variable arguments
            val argvEnd = args.size - argParamNumber + varArgsIndex
            val argv = args.subList(varArgsIndex, argvEnd).map { interpret(it, ref).obj }
            ref.set(varArg.name, ListObj(argv, startPos, endPos))

            // last part of arguments
            args.subList(argvEnd, args.size).forEachIndexed { i, it ->  ref.set(params[i + varArgsIndex + 1].name, interpret(it, ref).obj) }
        } else {
            if (args.size > params.size) throw TypeError("Too many arguments", startPos, endPos)
            args.forEachIndexed { i, it ->  ref.set(params[i].name, interpret(it, ref).obj)}
        }

        // keyword arguments
        if (varKwarg != null) {
            // normal keyword arguments
            kwargs.filter { it.key.value in params.map { param -> param.name } - varKwarg.name }.forEach { ref.set(it.key.value, interpret(it.value, ref).obj) }

            // variable kw arguments
            val kwargMap = mutableMapOf<String, BaseObject>()
            kwargs.filter { it.key.value !in params.map { param -> param.name } - varKwarg.name }.forEach { kwargMap[it.key.value] = interpret(it.value, ref).obj }
            ref.set(varKwarg.name, MapObj(kwargMap, startPos, endPos))
        } else {
            kwargs.forEach { ref.set(it.key.value, interpret(it.value, ref).obj) }
        }

        // check for any missing
        for (param in params) {
            if (ref.contain(param.name)) continue
            if (param.default != null) ref.set(param.name, param.default)
            else throw TypeError("Missing argument: ${param.name}", startPos, endPos)
        }

        return ref
    }

    private fun interpretPropAccess(node: PropAccessNode, ref: Referables): BaseObject {
        val obj = interpret(node.parent, ref).obj
        val prop = obj.property.getLocal(node.property.name) ?: throw AttributeError("Property \"${node.property.name}\" does not exist", node.startPos, node.endPos)

        return if (node.property.withCall) makeCall(prop, node.property.args, node.property.kwargs, ref, node.startPos, node.endPos)
        else prop
    }
}