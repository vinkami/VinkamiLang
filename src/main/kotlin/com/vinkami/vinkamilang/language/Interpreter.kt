package com.vinkami.vinkamilang.language

import com.vinkami.vinkamilang.language.exception.InterpretingException
import com.vinkami.vinkamilang.language.expression.BaseExpression
import com.vinkami.vinkamilang.language.expression.MathExpr
import com.vinkami.vinkamilang.language.expression.NumberExpr
import com.vinkami.vinkamilang.language.statement.IfStm
import com.vinkami.vinkamilang.language.statement.LoopStm
import com.vinkami.vinkamilang.language.statement.BaseStatement
import com.vinkami.vinkamilang.language.`object`.BaseObject
import com.vinkami.vinkamilang.language.`object`.NullObj
import com.vinkami.vinkamilang.language.`object`.NumberObj


class Interpreter(private val stm: BaseStatement) {
    private var globals: Map<String, Any> = mapOf()
    fun interpret(): String {
        return interpret(stm).toString()
    }

    private fun interpret(stm: BaseStatement): BaseObject {
        var result: BaseObject = NullObj()
        if (stm is BaseExpression) {
            result = interpretExpr(stm)
        } else {
            interpretStm(stm)
        }
        return result
    }

    private fun interpretExpr(expr: BaseExpression): BaseObject {
        val result = when (expr) {
            is MathExpr -> calcMath(expr)
            is NumberExpr -> calcNumber(expr)
            else -> throw InterpretingException("Unexpected expression: $expr", expr.position.startTokenPosition, "TODOError")
        }
        return result
    }

    private fun interpretStm(stm: BaseStatement) {
         when (stm) {
            is LoopStm -> runLoop(stm)
            is IfStm -> runIf(stm)

            else -> throw InterpretingException("Unexpected statement: $stm", stm.position.startTokenPosition, "TODOError")
        }
    }

    private fun calcMath(expr: MathExpr): BaseObject {
        val op = expr.op
        val lhs = interpret(expr.lhs)
        val rhs = interpret(expr.rhs)

        if (lhs !is NumberObj || rhs !is NumberObj) {
            throw InterpretingException("Non-number operations", expr.lhs.position.startTokenPosition, "TODOError")
        }

        val result = when (op.type) {
            TokenType.PLUS -> lhs.value + rhs.value
            TokenType.MINUS -> lhs.value - rhs.value
            TokenType.MULTIPLY -> lhs.value * rhs.value
            TokenType.DIVIDE -> lhs.value / rhs.value
            TokenType.MODULO -> lhs.value % rhs.value
            else -> throw InterpretingException("Unexpected operator: $op", op.position, "TODOError")
        }

        return NumberObj(result)
    }

    private fun calcNumber(expr: NumberExpr): BaseObject {
        val value = expr.value.value
        return NumberObj(value!!.toFloat())
    }

    private fun runIf(stm: IfStm) {
        TODO()
//        val conditionValue = interpret(stm.condition)
//        if (conditionValue is Boolean) {
//            if (conditionValue) {
//                return action.interpret()
//            } else {
//                for ((elifCondition, elifAction) in elif) {
//                    val elifConditionValue = elifCondition.interpret()
//                    if (elifConditionValue is Boolean) {
//                        if (elifConditionValue) {
//                            return elifAction.interpret()
//                        }
//                    } else {
//                        throw InterpretingException("Non-boolean condition in elif statement", condition.position.startTokenPosition, "TypeError")
//                    }
//                }
//                return fallback.interpret()
//            }
//        } else {
//            throw InterpretingException("Non-boolean condition in if statement", condition.position.startTokenPosition, "TypeError")
//        }
    }

    private fun runLoop(stm: LoopStm) {
        TODO()
    }
}