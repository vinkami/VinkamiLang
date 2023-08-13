package com.vinkami.vinkamilang.language.interpret.`object`.function

import com.vinkami.vinkamilang.language.exception.NotYourFaultError
import com.vinkami.vinkamilang.language.interpret.Referables
import com.vinkami.vinkamilang.language.interpret.`object`.BaseObject
import com.vinkami.vinkamilang.language.interpret.`object`.NullObj
import com.vinkami.vinkamilang.language.interpret.`object`.StringObj
import com.vinkami.vinkamilang.language.lex.Position

class PrintFunc: BuiltinFunc("print") {
    // prints the string representation of s to standard output
    // s: string to print; optional; defaults to ""
    // returns: null

    override val parameters = listOf(
        Parameter("s", null, StringObj("", startPos, endPos))
    )

    override operator fun invoke(ref: Referables, startPos: Position, endPos: Position): BaseObject {
        val stdout = ref.stdout
        if (stdout != null) {
            stdout(ref.get("s").toString())
        } else throw NotYourFaultError("No standard output", startPos, endPos)
        return NullObj(startPos, endPos)
    }
}