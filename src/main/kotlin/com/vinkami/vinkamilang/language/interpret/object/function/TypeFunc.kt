package com.vinkami.vinkamilang.language.interpret.`object`.function

import com.vinkami.vinkamilang.language.interpret.Referables
import com.vinkami.vinkamilang.language.interpret.`object`.BaseObject
import com.vinkami.vinkamilang.language.interpret.`object`.StringObj
import com.vinkami.vinkamilang.language.lex.Position

class TypeFunc: BuiltinFunc("type") {
    // obj: any non-null object; required
    // returns: type of obj as a string

    override val parameters = listOf(Parameter("obj", null, null))

    override operator fun invoke(ref: Referables, startPos: Position, endPos: Position): BaseObject {
        return StringObj(ref.get("obj")!!.type, startPos, endPos)
    }
}