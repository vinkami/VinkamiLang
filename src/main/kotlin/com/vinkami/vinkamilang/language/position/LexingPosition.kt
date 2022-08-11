package com.vinkami.vinkamilang.language.position

class LexingPosition(val filename: String, val lineNumber: Int,
                     val start: Int, val end: Int) {

    override fun toString(): String {
        return "In file $filename at line $lineNumber, from characters $start to $end"
    }

    companion object {
        val dummy = LexingPosition("", 0, 0, 0)
    }
}