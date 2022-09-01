package com.vinkami.vinkamilang.language.statement

import com.vinkami.vinkamilang.language.Token
import com.vinkami.vinkamilang.language.position.ParsingPosition

class BracketStm(val stm: BaseStatement, val bracL: Token, val bracR: Token, pos: ParsingPosition): BaseStatement(pos) {
    override fun toString(): String {
        return "($bracL $stm $bracR)"
    }
}