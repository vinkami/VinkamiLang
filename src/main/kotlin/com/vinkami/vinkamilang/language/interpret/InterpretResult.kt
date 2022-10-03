package com.vinkami.vinkamilang.language.interpret

import com.vinkami.vinkamilang.language.exception.BaseError
import com.vinkami.vinkamilang.language.exception.BaseInterrupt
import com.vinkami.vinkamilang.language.interpret.`object`.BaseObject

class InterpretResult() {
    lateinit var error: BaseError
    lateinit var obj: BaseObject
    var interrupt: BaseInterrupt? = null

    val hasError: Boolean get() = ::error.isInitialized
    val hasObject: Boolean get() = ::obj.isInitialized

    constructor(obj: BaseObject): this() {this(obj)}
    constructor(error: BaseError?): this() {this(error)}
    constructor(interrupt: BaseInterrupt): this() {this(interrupt)}

    operator fun invoke(obj: BaseObject) = apply {  // on success
        this.obj = obj
    }

    operator fun invoke(error: BaseError?) = apply {// on failure
        if (error != null) this.error = error
    }

    operator fun invoke(res: InterpretResult): InterpretResult {  // push error through nested parsing; won't make sense in a constructor
        if (res.hasError) this.error = res.error
        return res
    }

    operator fun invoke(interrupt: BaseInterrupt) = apply {  // on interrupt
        this.interrupt = interrupt
    }

    fun clearInterrupt() = apply {
        interrupt = null
    }
}