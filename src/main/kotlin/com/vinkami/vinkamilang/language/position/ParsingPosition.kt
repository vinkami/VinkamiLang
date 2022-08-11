package com.vinkami.vinkamilang.language.position

class ParsingPosition(val start: Int, val end: Int,
                      val startTokenPosition: LexingPosition, val endTokenPosition: LexingPosition) {
    override fun toString(): String {
        return "In file ${startTokenPosition.filename}" +
                "from line ${startTokenPosition.lineNumber} to line ${endTokenPosition.lineNumber}"
    }

    companion object {
        val dummy = ParsingPosition(0, 0, LexingPosition.dummy, LexingPosition.dummy)
    }
}
