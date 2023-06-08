package com.vinkami.vinkamilang.language.interpret

import com.vinkami.vinkamilang.language.exception.AssignmentError
import com.vinkami.vinkamilang.language.interpret.`object`.BaseObject
import com.vinkami.vinkamilang.language.interpret.`object`.function.PrintFunc
import com.vinkami.vinkamilang.language.interpret.`object`.function.TypeFunc

data class Referables(var stdout: ((String) -> Unit)? = null, private val isRoot: Boolean=true) {
    private var parent: Referables? = null
    private val variables: MutableMap<String, BaseObject> = mutableMapOf()
    private val constants: MutableMap<String, BaseObject> = mutableMapOf()

    init {
        if (isRoot) {
            setVal("print", PrintFunc())
            setVal("type", TypeFunc())
        }
    }

    fun get(name: String): BaseObject? {
        return constants[name] ?: variables[name] ?: parent?.get(name)
    }

    fun getLocal(name: String): BaseObject? {
        return constants[name] ?: variables[name]
    }

    fun contain(name: String): Boolean {
        return constants.containsKey(name) || variables.containsKey(name) || parent?.contain(name) ?: false
    }

//    fun containLocal(name: String): Boolean {
//        return variables.containsKey(name)
//    }

    fun set(name: String, value: BaseObject, mutable: Boolean): Referables {  // used when var/val is present; forces a creation of a new variable/constant
        return if (mutable) setVar(name, value) else setVal(name, value)
    }

    fun reassign(name: String, value: BaseObject): Referables {
        if (!contain(name)) throw AssignmentError("Cannot reassign non-existent variable '$name'", value.startPos, value.endPos)
        if (constants.containsKey(name)) throw AssignmentError("Cannot reassign constant '$name'", value.startPos, value.endPos)

        if (parent?.contain(name) == true) {
            parent?.reassign(name, value)
        } else {
            variables[name] = value
        }

        return this
    }

    private fun setVar(name: String, value: BaseObject): Referables {
        constants.remove(name)
        variables[name] = value
        return this
    }

    private fun setVal(name: String, value: BaseObject): Referables {
        variables.remove(name)
        constants[name] = value
        return this
    }

    fun bornChild(): Referables {
        val child = Referables(stdout, isRoot=false)
        child.parent = this
        return child
    }
}