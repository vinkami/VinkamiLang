package com.vinkami.vinkamilang.language.interpret

import com.vinkami.vinkamilang.language.Constant
import com.vinkami.vinkamilang.language.interpret.`object`.function.BuiltinFunc
import com.vinkami.vinkamilang.language.exception.*
import com.vinkami.vinkamilang.language.interpret.`object`.*
import com.vinkami.vinkamilang.language.interpret.`object`.function.Parameter
import com.vinkami.vinkamilang.language.lex.Position
import com.vinkami.vinkamilang.language.lex.Token
import com.vinkami.vinkamilang.language.lex.TokenType.*
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

        var obj = when (node) {
            is NumberNode -> interpretNumber(node)
            is StringNode -> StringObj(node.value, node.startPos, node.endPos)
            is BoolNode -> BoolObj(node.token.type == TRUE, node.startPos, node.endPos)
            is BinOpNode -> interpretBinOp(node, ref)
            is UnaryOpNode -> interpretUnaryOp(node, ref)
            is IdenNode -> interpretIden(node, ref)
            is AssignNode -> interpretAssign(node, ref)
            is ListNode -> interpretList(node, ref)
            is DictNode -> interpretDict(node, ref)
            is BracketNode -> interpretBracket(node, ref)
            is NullNode -> NullObj(node.startPos, node.endPos)
            is IfNode -> interpretIf(node, ref)
            is LoopNode -> interpretLoop(node, ref)
            is ProcedureNode -> interpretProcedural(node, ref)
            is FuncNode -> interpretFuncCreation(node, ref)
            is ClassNode -> interpretClassCreation(node, ref)
            is PropAccessNode -> interpretPropAccess(node, ref)

            is ArgumentsNode -> throw NotYourFaultError("ArgumentsNode should not be interpreted", node.startPos, node.endPos)
            else -> throw UnknownNodeError(node)
        }

        if (node.call != null) {
            val args = node.call!!.args
            val kwargs = node.call!!.kwargs
            val localRef = ref.bornChild()
            val startPos = node.startPos
            val endPos = node.endPos

            obj = when (obj) {
                is FuncObj -> interpretFunc(obj.node, args, kwargs, localRef, startPos, endPos)
                is BuiltinFunc -> interpretBultinFunc(obj, args, kwargs, localRef, startPos, endPos)
                is ClassObj -> interpretClass(obj.node, args, kwargs, localRef, startPos, endPos)
                else -> throw TypeError("${obj::class.simpleName} is not callable", startPos, endPos)
            }
        }

        return InterpretResult(obj)
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
                ASSIGN -> ref.set(name, value)
                PLUS_ASSIGN -> ref.set(name, ogValue.plus(value))
                MINUS_ASSIGN -> ref.set(name, ogValue.minus(value))
                MULTIPLY_ASSIGN -> ref.set(name, ogValue.times(value))
                DIVIDE_ASSIGN -> ref.set(name, ogValue.divide(value))
                MODULO_ASSIGN -> ref.set(name, ogValue.mod(value))
                POWER_ASSIGN -> ref.set(name, ogValue.power(value))
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
                PLUS -> left.plus(right)
                MINUS -> left.minus(right)
                MULTIPLY -> left.times(right)
                DIVIDE -> left.divide(right)
                MODULO -> left.mod(right)
                POWER -> left.power(right)

                EQUAL -> left.equal(right)
                NOT_EQUAL -> left.notEqual(right)
                LESS_EQUAL -> left.lessEqual(right)
                GREATER_EQUAL -> left.greaterEqual(right)
                LESS -> left.less(right)
                GREATER -> left.greater(right)

                AND -> left.and(right)
                OR -> left.or(right)

                else -> throw UnknownNodeError(node)
            }
        }
    }

    private fun interpretUnaryOp(node: UnaryOpNode, ref: Referables): BaseObject {
        val innerObj = interpret(node.innerNode, ref).obj

        return when (node.op.type) {
            PLUS -> innerObj.unaryPlus()
            MINUS -> innerObj.unaryMinus()
            NOT -> innerObj.not()
            else -> throw UnknownNodeError(node)
        }
    }

    private fun interpretIden(node: IdenNode, ref: Referables): BaseObject {
        return ref.get(node.name) ?: throw NameError("Undefined name \"${node.name}\"", node.startPos, node.endPos)
    }


    private fun interpretAssign(node: AssignNode, ref: Referables): BaseObject {
        val value = interpret(node.value, ref).obj

        ref.set(node.iden.name, value)
        return NullObj(node.startPos, node.endPos)
    }

    private fun interpretList(node: ListNode, ref: Referables): BaseObject {
        val list = mutableListOf<BaseObject>()
        for (item in node.nodes) {
            list.add(interpret(item, ref).obj)
        }
        return ListObj(list, node.startPos, node.endPos)
    }

    private fun interpretDict(node: DictNode, ref: Referables): BaseObject {
        val dict = mutableMapOf<BaseObject, BaseObject>()
        for ((key, value) in node.dict) {
            dict[StringObj(key.value, key.startPos, key.endPos)] = interpret(value, ref).obj
        }
        return DictObj(dict, node.startPos, node.endPos)
    }

    private fun interpretBracket(node: BracketNode, ref: Referables): BaseObject {
        return interpret(node.innerNode, ref).obj
    }

    private fun interpretIf(node: IfNode, ref: Referables): BaseObject {
        val localRef = ref.bornChild()

        val cond = interpret(node.condition, localRef).obj
        if (cond.boolVal) {
            return interpret(node.action, localRef).obj
        }

        for ((elifCondNode, elifActionNode) in node.elif) {
            val elifCond = interpret(elifCondNode, localRef).obj
            if (elifCond.boolVal) {
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
        if (node.loopTokenType == WHILE) {
            var cond = interpret(node.condition, ref).obj

            while (cond.boolVal) {
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
        val obj = FuncObj(node)
        ref.set(node.name.value, obj)
        return obj
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
        val obj = ClassObj(node)
        ref.set(node.name.value, obj)
        return obj
    }

    private fun interpretClass(node: ClassNode, args: List<BaseNode>, kwargs: Map<Token, BaseNode>, ref: Referables, startPos: Position, endPos: Position): BaseObject {
        val thisRef = setParams(node.initParams.map { convertParamNode(it, ref) }, args, kwargs, ref, startPos, endPos)

        // inheritance
        node.parent?.let {
            val parent = interpret(it, thisRef).obj
            thisRef.set("that", parent)
        }

        // set class methods, constants, etc. and does the init work
        interpret(node.body, thisRef)
        val thisObj = CustomObj(node.name.value, thisRef, node.startPos, node.endPos)
        thisRef.set("this", thisObj)

        return thisObj
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

        return obj.property.getLocal(node.property.name)
            ?: obj.property.getLocal("that")?.property?.getLocal(node.property.name)  // class inheritance
            ?: throw AttributeError("Property \"${node.property.name}\" does not exist", node.startPos, node.endPos)
    }
}