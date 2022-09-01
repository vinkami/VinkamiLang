package com.vinkami.vinkamilang.language.statement

import com.vinkami.vinkamilang.language.TokenType
import com.vinkami.vinkamilang.language.expression.BaseExpression
import com.vinkami.vinkamilang.language.expression.NullExpr
import com.vinkami.vinkamilang.language.position.ParsingPosition

class LoopStm(val loopType: TokenType, val condition: BaseExpression, val mainAction: BaseStatement,
              val completeAction: BaseStatement, val incompleteAction: BaseStatement,
              pos: ParsingPosition): BaseStatement(pos) {

    override fun toString(): String {
        val sb = StringBuilder()
        if (loopType == TokenType.WHILE) sb.append("(while ") else sb.append("(for ")
          sb.append(condition)
            .append(" -> ")
            .append(mainAction)

        if (completeAction !is NullExpr) {
            sb.append(" | complete -> ")
              .append(completeAction)
        }

        if (incompleteAction !is NullExpr) {
            sb.append(" | incomplete -> ")
              .append(incompleteAction)
        }

        sb.append(")")
        return sb.toString()
    }
}