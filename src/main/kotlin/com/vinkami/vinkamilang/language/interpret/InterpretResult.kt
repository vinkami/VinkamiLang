package com.vinkami.vinkamilang.language.interpret

import com.vinkami.vinkamilang.language.exception.NotYourFaultError
import com.vinkami.vinkamilang.language.interpret.`object`.BaseObject
import com.vinkami.vinkamilang.language.interpret.`object`.NullObj
import com.vinkami.vinkamilang.language.lex.TokenType

class InterpretResult(val obj: BaseObject, var interrupt: TokenType? = null) {
    // interrupt: TT.BREAK / TT.RETURN

    init {
        if (interrupt !in listOf(null, TokenType.BREAK, TokenType.RETURN)) throw NotYourFaultError("Invalid interrupt type: $interrupt", obj.startPos, obj.endPos)
    }

    val hasInterrupt: Boolean
        get() = interrupt != null

    val hasObject: Boolean
        get() = obj !is NullObj

    fun clearInterrupt() = apply {
        interrupt = null
    }
}