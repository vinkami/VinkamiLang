package com.vinkami.vinkamilang.language.parse.node

import com.vinkami.vinkamilang.language.lex.Position

class AssignNode(val iden: IdenNode, val value: BaseNode, val mutable: Boolean, startPos: Position): BaseNode(startPos, value.endPos) {
    override fun toString(): String {
        return "(${if (mutable) "var" else "val"} $iden = $value)$callStr"
    }
}