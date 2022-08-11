package com.vinkami.vinkamilang.language.expression

import com.vinkami.vinkamilang.language.position.ParsingPosition

class While(val condition: Expression, val mainAction: Expression,
            val completeAction: Expression, val incompleteAction: Expression,
            pos: ParsingPosition): Expression(pos) {

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("(while ")
          .append(condition)
          .append(" -> ")
          .append(mainAction)

        if (completeAction !is Null) {
            sb.append(" | complete -> ")
              .append(completeAction)
        }

        if (incompleteAction !is Null) {
            sb.append(" | incomplete -> ")
              .append(incompleteAction)
        }

        sb.append(")")
        return sb.toString()
    }
}