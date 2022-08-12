package com.vinkami.vinkamilang.language.expression

import com.vinkami.vinkamilang.language.exception.InterpretingException
import com.vinkami.vinkamilang.language.exception.ParsingException
import com.vinkami.vinkamilang.language.position.ParsingPosition

class If(val condition: Expression, val action: Expression,
         val elif: Map<Expression, Expression>, val fallback: Expression,
         pos: ParsingPosition): Expression(pos) {
    override fun toString(): String {
      // Structure: (if (condition) -> (action) | (condition) -> (action) | (fallback))
        val sb = StringBuilder()
        sb.append("(if ")
          .append(condition)
          .append(" -> ")
          .append(action)

        for ((elifCondition, elifAction) in elif) {
            sb.append(" | ")
              .append(elifCondition)
              .append(" -> ")
              .append(elifAction)
        }

        if (fallback !is Null) {
            sb.append(" | (")
              .append(fallback)
              .append(")")
        }
        sb.append(")")
        return sb.toString()
    }

//    override fun interpret(): Any {
//        val conditionValue = condition.interpret()
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
//    }
}