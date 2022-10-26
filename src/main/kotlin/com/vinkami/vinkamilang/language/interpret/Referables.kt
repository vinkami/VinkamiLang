package com.vinkami.vinkamilang.language.interpret

import com.vinkami.vinkamilang.language.interpret.`object`.BaseObject

data class Referables(val variables: MutableMap<String, BaseObject> = mutableMapOf()) {
    var parent: Referables? = null

    fun get(name: String): BaseObject? {
        return variables[name] ?: parent?.get(name)
    }

    fun set(name: String, value: BaseObject) {
        if (parent?.get(name) != null) {
            parent?.set(name, value)
        } else {
            variables[name] = value
        }
    }

    fun bornChild(): Referables {
        val child = Referables()
        child.parent = this
        return child
    }
}