package com.vinkami.vinkamilang.language.interpret.`object`.builtin

import com.vinkami.vinkamilang.language.exception.NotYourFaultError
import com.vinkami.vinkamilang.language.interpret.Referables
import com.vinkami.vinkamilang.language.interpret.`object`.BaseObject
import com.vinkami.vinkamilang.language.interpret.`object`.NullObj
import com.vinkami.vinkamilang.language.interpret.`object`.StringObj

class PrintFunc: BuiltinFunc("print") {
    override val parameters = listOf(Parameter("s", null, StringObj("", this.startPos, this.endPos)))

    override operator fun invoke(ref: Referables): BaseObject {
        val stdout = ref.stdout
        if (stdout != null) {
            stdout(ref.get("s").toString())
        } else throw NotYourFaultError("No standard output", this.startPos, this.endPos)
        return NullObj(this.startPos, this.endPos)
    }
}