package com.vinkami.vinkamilang.language.expression

import com.vinkami.vinkamilang.language.TokenType
import com.vinkami.vinkamilang.language.position.ParsingPosition

class Loop(val loopType: TokenType, val condition: Expression, val mainAction: Expression,
           val completeAction: Expression, val incompleteAction: Expression,
           pos: ParsingPosition): Expression(pos) {

//    override fun toString(): String {
//        val sb = StringBuilder()
//        if (loopType == TokenType.WHILE) sb.append("(while ") else sb.append("(for ")
//          .append(condition)
//          .append(" -> ")
//          .append(mainAction)
//
//        if (completeAction !is Null) {
//            sb.append(" | complete -> ")
//              .append(completeAction)
//        }
//
//        if (incompleteAction !is Null) {
//            sb.append(" | incomplete -> ")
//              .append(incompleteAction)
//        }
//
//        sb.append(")")
//        return sb.toString()
//    }
}