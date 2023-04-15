package com.vinkami.vinkamilang.language.interpret

import com.vinkami.vinkamilang.language.interpret.`object`.BaseObject
import com.vinkami.vinkamilang.language.interpret.`object`.builtin.PrintFunc
import com.vinkami.vinkamilang.language.interpret.`object`.builtin.TypeFunc

data class Referables(var stdout: ((String) -> Unit)?, private val variables: MutableMap<String, BaseObject> = mutableMapOf(), private val isRoot: Boolean=true) {
    private var parent: Referables? = null

    init {
        if (isRoot) {
            set("print", PrintFunc())
            set("type", TypeFunc())
        }
    }

    fun get(name: String): BaseObject? {
        return variables[name] ?: parent?.get(name)
    }

    fun dotGet(name: String): BaseObject? {
        return variables[name]
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
}