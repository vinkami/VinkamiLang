package com.vinkami.vinkamilang.language.interpret

import com.vinkami.vinkamilang.language.exception.BaseError
import com.vinkami.vinkamilang.language.interpret.`object`.BaseObject
import com.vinkami.vinkamilang.language.lex.TokenType

class InterpretResult() {
    lateinit var error: BaseError
    private lateinit var _obj: BaseObject

    val obj: BaseObject
        get() {
            if (::_obj.isInitialized) return _obj
            if (hasError) throw error
            throw IllegalAccessError("Accessing boject of an interpret() but got no object nor error. Find vinkami for help.")
        }
    var interrupt: TokenType? = null  // BREAK / RETURN

    val hasError: Boolean get() = ::error.isInitialized
    val hasObject: Boolean get() {
        if (::_obj.isInitialized) return true
        if (hasError) throw error
        return false
    }

    constructor(obj: BaseObject) : this() { this(obj) }

    operator fun invoke(obj: BaseObject) = apply {  // on success
        if (obj is BaseError) {
            this.error = obj
        } else {
            this._obj = obj
        }
    }
}