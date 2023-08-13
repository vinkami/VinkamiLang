package com.vinkami.vinkamilang.language.interpret

import com.vinkami.vinkamilang.language.interpret.`object`.BaseObject
import com.vinkami.vinkamilang.language.interpret.`object`.NullObj
import com.vinkami.vinkamilang.language.lex.TokenType

class InterpretResult(val obj: BaseObject, var interrupt: TokenType? = null) {
    // interrupt: TT.BREAK / TT.RETURN

    val hasInterrupt: Boolean
        get() = interrupt != null

    val hasObject: Boolean
        get() = obj !is NullObj

    fun clearInterrupt() = apply {
        interrupt = null
    }
}