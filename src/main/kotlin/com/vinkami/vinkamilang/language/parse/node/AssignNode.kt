package com.vinkami.vinkamilang.language.parse.node

import com.vinkami.vinkamilang.language.lex.Position
import com.vinkami.vinkamilang.language.lex.Token

class AssignNode(val iden: IdenNode, val assignToken: Token, val value: BaseNode, startPos: Position): BaseNode(startPos, value.endPos) {
    override fun toString(): String {
        return "($iden $assignToken $value)$callStr"
    }
}