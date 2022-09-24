package com.vinkami.vinkamilang.language.interpret

import com.vinkami.vinkamilang.language.exception.BaseLangException
import com.vinkami.vinkamilang.language.interpret.`object`.BaseObject

class InterpretResult() {
    lateinit var error: BaseLangException
    lateinit var obj: BaseObject

    val hasError: Boolean get() = ::error.isInitialized
    val hasObject: Boolean get() = ::obj.isInitialized

    constructor(obj: BaseObject): this() {this(obj)}
    constructor(error: BaseLangException?): this() {this(error)}

    operator fun invoke(obj: BaseObject) = apply {  // on success
        this.obj = obj
    }

    operator fun invoke(error: BaseLangException?) = apply {// on failure
        if (error != null) this.error = error
    }

    operator fun invoke(res: InterpretResult): InterpretResult {  // push error through nested parsing; won't make sense in a constructor
        if (res.hasError) this.error = res.error
        return res
    }
}