package com.vinkami.vinkamilang.language.interpret.`object`.builtin

import com.vinkami.vinkamilang.language.interpret.Referables
import com.vinkami.vinkamilang.language.interpret.`object`.BaseObject
import com.vinkami.vinkamilang.language.interpret.`object`.StringObj

class ListOf: BuiltinFunc("arrayOf") {
    override val parameters = listOf(Parameter("Arguments", null, null, true))
    override operator fun invoke(ref: Referables): BaseObject {
        return StringObj(ref.get("obj")!!.type, this.startPos, this.endPos)
    }
}