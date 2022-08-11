package com.vinkami.vinkamilang.language.expression

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
}