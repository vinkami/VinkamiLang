package com.vinkami.vinkamilang.language.expression

import com.vinkami.vinkamilang.language.position.ParsingPosition
import com.vinkami.vinkamilang.language.statement.BaseStatement

class NullExpr: BaseStatement(ParsingPosition.dummy) {
    override fun toString(): String {
        return "null"
    }
}
