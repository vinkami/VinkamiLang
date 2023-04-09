package com.vinkami.vinkamilang.language.interpret

import com.vinkami.vinkamilang.language.interpret.`object`.BaseObject
import com.vinkami.vinkamilang.language.interpret.`object`.builtin.PrintFunc

data class Referables(var stdout: ((String)-> Unit)?, val variables: MutableMap<String, BaseObject> = mutableMapOf(), val isRoot: Boolean=true) {
    private var parent: Referables? = null
    private var tempParent: Referables? = null

    init {
        if (isRoot) {
            set("print", PrintFunc())
        }
    }

    fun get(name: String): BaseObject? {
        return variables[name] ?: parent?.get(name) ?: tempParent?.get(name)
    }

    fun set(name: String, value: BaseObject): Referables {
        if (parent?.get(name) != null) {
            parent?.set(name, value)
        } else {
            variables[name] = value
        }
        return this
    }

    fun bornChild(): Referables {
        val child = Referables(stdout, isRoot=false)
        child.parent = this
        return child
    }

    fun withTempParent(parent: Referables): Referables {
        tempParent = parent
        return this
    }

    fun removeTempParent() {
        tempParent = null
    }
}