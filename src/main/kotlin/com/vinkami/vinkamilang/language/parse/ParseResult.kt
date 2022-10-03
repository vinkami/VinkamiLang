package com.vinkami.vinkamilang.language.parse

import com.vinkami.vinkamilang.language.exception.BaseError
import com.vinkami.vinkamilang.language.parse.node.BaseNode

class ParseResult() {
    lateinit var error: BaseError
    lateinit var node: BaseNode

    val hasError: Boolean get() = ::error.isInitialized
    val hasNode: Boolean get() = ::node.isInitialized

    constructor(node: BaseNode): this() {this(node)}
    constructor(error: BaseError?): this() {this(error)}

    operator fun invoke(node: BaseNode) = apply {  // on success
        this.node = node
    }

    operator fun invoke(error: BaseError?) = apply {// on failure
        if (error != null) this.error = error
    }

    operator fun invoke(res: ParseResult): ParseResult {  // push error through nested parsing; won't make sense in a constructor
        if (res.hasError) this.error = res.error
        return res
    }
}
