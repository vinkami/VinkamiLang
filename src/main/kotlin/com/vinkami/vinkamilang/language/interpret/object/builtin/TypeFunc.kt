package com.vinkami.vinkamilang.language.interpret.`object`.builtin

import com.vinkami.vinkamilang.language.interpret.Referables
import com.vinkami.vinkamilang.language.interpret.`object`.BaseObject
import com.vinkami.vinkamilang.language.interpret.`object`.StringObj

class TypeFunc: BuiltinFunc("type") {
    override val parameters = listOf(Parameter("obj", null, null))

    override operator fun invoke(ref: Referables): BaseObject {
        return StringObj(ref.get("obj")!!.type, this.startPos, this.endPos)
    }
}