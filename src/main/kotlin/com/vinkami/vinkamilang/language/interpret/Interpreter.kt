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

    private fun interpretNoInterrupt(node: BaseNode, ref: Referables): InterpretResult {
        val res = interpret(node, ref)

        if (res.interrupt != null) {
            throw SyntaxError("Interrupt should not occur here", node.startPos, node.endPos)
        }

        return res
    }

    private fun interpret(node: BaseNode, ref: Referables): InterpretResult {
        node::class.simpleName ?: return InterpretResult(UnknownNodeError(node))

        var res: InterpretResult = when (node) {
            is NumberNode -> interpretNumber(node)
            is StringNode -> InterpretResult(StringObj(node.value, node.startPos, node.endPos))
            is BoolNode -> InterpretResult(BoolObj(node.token.type == TRUE, node.startPos, node.endPos))
            is BinOpNode -> interpretBinOp(node, ref)
            is UnaryOpNode -> interpretUnaryOp(node, ref)
            is IdenNode -> interpretIden(node, ref)
            is AssignNode -> interpretAssign(node, ref)
            is ListNode -> interpretList(node, ref)
            is DictNode -> interpretDict(node, ref)
            is BracketNode -> interpretBracket(node, ref)
            is NullNode -> InterpretResult(NullObj(node.startPos, node.endPos))
            is IfNode -> interpretIf(node, ref)
            is LoopNode -> interpretLoop(node, ref)
            is InterruptNode -> interpretInterrupt(node, ref)
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

            res = when (res.obj) {
                is FuncObj -> interpretFunc((res.obj as FuncObj).node, args, kwargs, localRef, startPos, endPos)
                is BuiltinFunc -> interpretBultinFunc(res.obj as BuiltinFunc, args, kwargs, localRef, startPos, endPos)
                is ClassObj -> interpretClass((res.obj as ClassObj).node, args, kwargs, localRef, startPos, endPos)
                else -> throw TypeError("${res.obj::class.simpleName} is not callable", startPos, endPos)
            }
        }

        return res
    }

    private fun interpretInterrupt(node: InterruptNode, ref: Referables): InterpretResult {
        val res = InterpretResult()
        res.interrupt = node.type
        val obj = interpretNoInterrupt(node.innerNode, ref).obj
        return res(obj)
    }

    private fun interpretNumber(node: NumberNode): InterpretResult {
        val valueString = node.value
        try {
            val value = valueString.toFloat()
            return InterpretResult(NumberObj(value, node.startPos, node.endPos))
        } catch (e: NumberFormatException) {
            throw SyntaxError("Invalid number: $valueString", node.startPos, node.endPos)
        }
    }

    private fun interpretBinOp(node: BinOpNode, ref: Referables): InterpretResult {
        if (node.op.type in Constant.difinitiveOp) {
            // Variable assignment
            if (node.left !is IdenNode) throw SyntaxError("Invalid assignment", node.startPos, node.endPos)
            val name = node.left.name
            val value = interpretNoInterrupt(node.right, ref).obj
            val ogValue =
                ref.get(name) ?: throw NameError("Unknown variable $name", node.left.startPos, node.left.endPos)

            when (node.op.type) {
                ASSIGN -> ref.reassign(name, value)
                PLUS_ASSIGN -> ref.reassign(name, ogValue.plus(value))
                MINUS_ASSIGN -> ref.reassign(name, ogValue.minus(value))
                MULTIPLY_ASSIGN -> ref.reassign(name, ogValue.times(value))
                DIVIDE_ASSIGN -> ref.reassign(name, ogValue.divide(value))
                MODULO_ASSIGN -> ref.reassign(name, ogValue.mod(value))
                POWER_ASSIGN -> ref.reassign(name, ogValue.power(value))
                else -> throw NotYourFaultError(
                    "Invalid assignment operator ${node.op.type}",
                    node.op.startPos,
                    node.op.endPos
                )  // No other TT are allowed from parser
            }
            return InterpretResult(NullObj(node.startPos, node.endPos))

        } else {
            // Normal caluclation
            val left = interpretNoInterrupt(node.left, ref).obj
            val right = interpretNoInterrupt(node.right, ref).obj

            val obj = when (node.op.type) {
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

            return InterpretResult(obj)
        }
    }

    private fun interpretUnaryOp(node: UnaryOpNode, ref: Referables): InterpretResult {
        val innerObj = interpretNoInterrupt(node.innerNode, ref).obj

        val obj =  when (node.op.type) {
            PLUS -> innerObj.unaryPlus()
            MINUS -> innerObj.unaryMinus()
            NOT -> innerObj.not()
            else -> throw UnknownNodeError(node)
        }

        return InterpretResult(obj)
    }

    private fun interpretIden(node: IdenNode, ref: Referables): InterpretResult {
        return InterpretResult(ref.get(node.name) ?: throw NameError("Undefined name \"${node.name}\"", node.startPos, node.endPos))
    }

    private fun interpretAssign(node: AssignNode, ref: Referables): InterpretResult {
        val value = interpretNoInterrupt(node.value, ref).obj

        ref.set(node.iden.name, value, node.mutable)
        return InterpretResult(NullObj(node.startPos, node.endPos))
    }

    private fun interpretList(node: ListNode, ref: Referables): InterpretResult {
        val list = mutableListOf<BaseObject>()
        for (item in node.nodes) {
            list.add(interpretNoInterrupt(item, ref).obj)
        }
        return InterpretResult(ListObj(list, node.startPos, node.endPos))
    }

    private fun interpretDict(node: DictNode, ref: Referables): InterpretResult {
        val dict = mutableMapOf<BaseObject, BaseObject>()
        for ((key, value) in node.dict) {
            dict[StringObj(key.value, key.startPos, key.endPos)] = interpretNoInterrupt(value, ref).obj
        }
        return InterpretResult(DictObj(dict, node.startPos, node.endPos))
    }

    private fun interpretBracket(node: BracketNode, ref: Referables): InterpretResult {
        return interpret(node.innerNode, ref)
    }

    private fun interpretIf(node: IfNode, ref: Referables): InterpretResult {
        val localRef = ref.bornChild()

        val cond = interpretNoInterrupt(node.condition, localRef).obj
        if (cond.boolVal) {
            return interpret(node.action, localRef)
        }

        for ((elifCondNode, elifActionNode) in node.elif) {
            val elifCond = interpretNoInterrupt(elifCondNode, localRef).obj
            if (elifCond.boolVal) {
                return interpret(elifActionNode, localRef)
            }
        }

        if (node.elseAction != null) {
            return interpret(node.elseAction, localRef)
        }

        return InterpretResult(NullObj(node.startPos, node.endPos))
    }

    // TODO: Add for loop
    private fun interpretLoop(node: LoopNode, globalRef: Referables): InterpretResult {
        val ref = globalRef.bornChild()
        var finalObj: BaseObject = NullObj(node.startPos, node.endPos)

        var complete = true
        if (node.loopTokenType == WHILE) {
            var cond = interpretNoInterrupt(node.condition, ref).obj

            while (cond.boolVal) {
                val res = interpret(node.mainAction, ref)
                if (res.interrupt == BREAK) {
                    complete = false
                    break
                }
                if (res.interrupt == RETURN) {
                    return res
                }
                finalObj = res.obj
                cond = interpretNoInterrupt(node.condition, ref).obj
            }

        } else throw NotYourFaultError("Unknown loop token type: ${node.loopTokenType}", node.startPos, node.endPos)

        if (complete) {
            val compRes = interpret(node.compAction ?: NullNode(node.startPos, node.endPos), ref).also { if (it.interrupt != null) return it }
            if (compRes.hasObject) finalObj = compRes.obj
        } else {
            val incompRes = interpret(node.incompAction ?: NullNode(node.startPos, node.endPos), ref).also { if (it.interrupt != null) return it }
            if (incompRes.hasObject) finalObj = incompRes.obj
        }

        return InterpretResult(finalObj)
    }

    private fun interpretProcedural(node: ProcedureNode, ref: Referables): InterpretResult {
        var finalObj: BaseObject = NullObj(node.startPos, node.endPos)
        for (procedure in node.procedures) {
            finalObj = interpret(procedure, ref).also { if (it.interrupt != null) return it }.obj
        }
        return InterpretResult(finalObj)
    }

    private fun interpretFuncCreation(node: FuncNode, ref: Referables): InterpretResult {
        val obj = FuncObj(node)
        ref.set(node.name.value, obj, false)
        return InterpretResult(obj)
    }

    private fun interpretFunc(node: FuncNode, args: List<BaseNode>, kwargs: Map<Token, BaseNode>, ref: Referables, startPos: Position, endPos: Position): InterpretResult {
        val params = node.params.map { convertParamNode(it, ref) }
        setParams(params, args, kwargs, ref, startPos, endPos)
        return interpret(node.body, ref)
    }

    private fun interpretBultinFunc(obj: BuiltinFunc, args: List<BaseNode>, kwargs: Map<Token, BaseNode>, ref: Referables, startPos: Position, endPos: Position): InterpretResult {  // BuiltinFunc doesn't have positional information
        setParams(obj.parameters, args, kwargs, ref, startPos, endPos)
        return InterpretResult(obj(ref))
    }

    private fun interpretClassCreation(node: ClassNode, ref: Referables): InterpretResult {
        val obj = ClassObj(node)
        ref.set(node.name.value, obj, false)
        return InterpretResult(obj)
    }

    private fun interpretClass(node: ClassNode, args: List<BaseNode>, kwargs: Map<Token, BaseNode>, ref: Referables, startPos: Position, endPos: Position): InterpretResult {
        val thisRef = setParams(node.initParams.map { convertParamNode(it, ref) }, args, kwargs, ref, startPos, endPos)

        // inheritance
        node.parent?.let {
            val parent = interpretNoInterrupt(it, thisRef).obj
            thisRef.set("that", parent, false)
        }

        // set class methods, constants, etc. and does the init work
        interpretNoInterrupt(node.body, thisRef)
        val thisObj = CustomObj(node.name.value, thisRef, node.startPos, node.endPos)
        thisRef.set("this", thisObj, false)

        return InterpretResult(thisObj)
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
            args.subList(0, varArgsIndex).forEachIndexed { i, it ->  ref.set(params[i].name, interpret(it, ref).obj, false) }

            // variable arguments
            val argvEnd = args.size - argParamNumber + varArgsIndex
            val argv = args.subList(varArgsIndex, argvEnd).map { interpret(it, ref).obj }
            ref.set(varArg.name, ListObj(argv, startPos, endPos), false)

            // last part of arguments
            args.subList(argvEnd, args.size).forEachIndexed { i, it ->  ref.set(params[i + varArgsIndex + 1].name, interpret(it, ref).obj, false) }
        } else {
            if (args.size > params.size) throw TypeError("Too many arguments", startPos, endPos)
            args.forEachIndexed { i, it ->  ref.set(params[i].name, interpret(it, ref).obj, false)}
        }

        // keyword arguments
        if (varKwarg != null) {
            // normal keyword arguments
            kwargs.filter { it.key.value in params.map { param -> param.name } - varKwarg.name }.forEach { ref.set(it.key.value, interpret(it.value, ref).obj, false) }

            // variable kw arguments
            val kwargMap = mutableMapOf<String, BaseObject>()
            kwargs.filter { it.key.value !in params.map { param -> param.name } - varKwarg.name }.forEach { kwargMap[it.key.value] = interpret(it.value, ref).obj }
            ref.set(varKwarg.name, MapObj(kwargMap, startPos, endPos), false)
        } else {
            kwargs.forEach { ref.set(it.key.value, interpret(it.value, ref).obj, false) }
        }

        // check for any missing
        for (param in params) {
            if (ref.contain(param.name)) continue
            if (param.default != null) ref.set(param.name, param.default, false)
            else throw TypeError("Missing argument: ${param.name}", startPos, endPos)
        }

        return ref
    }

    private fun interpretPropAccess(node: PropAccessNode, ref: Referables): InterpretResult {
        val obj = interpretNoInterrupt(node.parent, ref).obj

        val prop = obj.property.getLocal(node.property.name)
            ?: obj.property.getLocal("that")?.property?.getLocal(node.property.name)  // class inheritance
            ?: throw AttributeError("Property \"${node.property.name}\" does not exist", node.startPos, node.endPos)

        return InterpretResult(prop)
    }
}