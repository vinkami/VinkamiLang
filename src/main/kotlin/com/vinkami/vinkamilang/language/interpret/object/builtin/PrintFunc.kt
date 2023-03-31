package com.vinkami.vinkamilang.language.interpret.`object`.builtin

import com.vinkami.vinkamilang.language.interpret.Referables
import com.vinkami.vinkamilang.language.interpret.`object`.BaseObject
import com.vinkami.vinkamilang.language.interpret.`object`.NullObj

class PrintFunc: BuiltinFunc("print") {
    override val parameters = listOf(Parameter("s", null, null))

    override operator fun invoke(ref: Referables): BaseObject {
        val stdout = ref.stdout
        if (stdout != null) {
            stdout(ref.get("s").toString())
        }
        return NullObj(this.startPos, this.endPos)
    }
}