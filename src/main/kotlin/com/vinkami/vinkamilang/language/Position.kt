package com.vinkami.vinkamilang.language

class Position(val filename: String, val lineNumber: Int,
               val start: Int, val end: Int) {

    override fun toString(): String {
        return "In file $filename at line $lineNumber, from characters $start to $end"
    }
}
