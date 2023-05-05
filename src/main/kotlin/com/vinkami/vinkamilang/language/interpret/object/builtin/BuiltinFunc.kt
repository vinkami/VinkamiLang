package com.vinkami.vinkamilang.language.interpret.`object`.builtin

import com.vinkami.vinkamilang.language.Constant.builtinPos
import com.vinkami.vinkamilang.language.interpret.Referables
import com.vinkami.vinkamilang.language.interpret.`object`.BaseObject

abstract class BuiltinFunc(val name: String): BaseObject {
    override val type = "Function"
    override val value = name
    override val startPos = builtinPos
    override val endPos = builtinPos
    override val property = Referables(null, isRoot=false)

    override fun toString(): String {
        return "<function $name>"
    }

    abstract operator fun invoke(ref: Referables): BaseObject
    abstract val parameters: List<Parameter>
}