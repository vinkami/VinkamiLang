package com.vinkami.vinkamilang.language

import com.vinkami.vinkamilang.language.exception.InterpretingException
import com.vinkami.vinkamilang.language.expression.*
import com.vinkami.vinkamilang.language.`object`.*


class Interpreter(private val expr: Expression) {
    private var globals: Map<String, Any> = mapOf()
    fun interpret() {
        interpret(expr)
    }

    private fun interpret(expr: Expression){  // So that the little expressions can share globals
        val result = when (expr) {
            is Math -> runMath()

            else -> throw InterpretingException("Unexpected expression: $expr", expr.position.startTokenPosition, "TODOError")
        }

    }

    private fun runMath() {
        TODO()
    }

    private fun runIf() {
        TODO()
    }

    private fun runLoop(expr: Loop) {
        TODO()
    }
}