package com.vinkami.vinkamilang.language.interpret

import com.vinkami.vinkamilang.language.interpret.`object`.BaseObject

data class Referables(val variables: MutableMap<String, BaseObject> = mutableMapOf()) {
    var parent: Referables? = null

    fun locate(name: String): BaseObject? {
        return variables[name] ?: parent?.locate(name)
    }

    fun assign(name: String, value: BaseObject) {
        if (parent?.locate(name) != null) {
            parent?.assign(name, value)
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