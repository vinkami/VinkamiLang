package com.vinkami.vinkamilang.language.interpret.`object`.function

import com.vinkami.vinkamilang.language.Constant.builtinPos
import com.vinkami.vinkamilang.language.interpret.Referables
import com.vinkami.vinkamilang.language.interpret.`object`.BaseObject
import com.vinkami.vinkamilang.language.lex.Position

abstract class BuiltinFunc(val name: String): BaseObject {
    override val type = "Function"
    override val value = name
    override val startPos = builtinPos  // these startPos and endPos are to act as a placeholder for default values of parameters.
    override val endPos = builtinPos    // the actual startPos and endPos are passed in the invoke() function.
    override val property = Referables(null, isRoot=false)

    override fun toString(): String {
        return "<function $name>"
    }

    abstract operator fun invoke(ref: Referables, startPos: Position, endPos: Position): BaseObject
    abstract val parameters: List<Parameter>
}