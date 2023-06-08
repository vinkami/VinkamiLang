package com.vinkami.vinkamilang.language.interpret

import com.vinkami.vinkamilang.language.exception.BaseError
import com.vinkami.vinkamilang.language.interpret.`object`.BaseObject
import com.vinkami.vinkamilang.language.lex.TokenType

class InterpretResult() {
    lateinit var error: BaseError
    lateinit var obj: BaseObject
    var interrupt: TokenType? = null  // BREAK / RETURN

    val hasError: Boolean get() = ::error.isInitialized
    val hasObject: Boolean get() = ::obj.isInitialized

    constructor(error: BaseError?): this() {this(error)}

    operator fun invoke(obj: BaseObject) = apply {  // on success
        this.obj = obj
    }

    operator fun invoke(error: BaseError?) = apply {// on failure
        if (error != null) this.error = error
    }
}