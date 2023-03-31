package com.vinkami.vinkamilang.language.interpret.`object`.builtin

import com.vinkami.vinkamilang.language.interpret.Referables
import com.vinkami.vinkamilang.language.interpret.`object`.BaseObject
import com.vinkami.vinkamilang.language.lex.Position

abstract class BuiltinFunc(val name: String): BaseObject {
    override val value = name
    override val startPos = Position(-1, -1, -1, "builtin", "builtin")
    override val endPos = Position(-1, -1, -1, "builtin", "builtin")

    override fun toString(): String {
        return "<function $name>"
    }

    abstract operator fun invoke(ref: Referables): BaseObject
    abstract val parameters: List<Parameter>
}