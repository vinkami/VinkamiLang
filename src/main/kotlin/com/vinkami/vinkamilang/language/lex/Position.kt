package com.vinkami.vinkamilang.language.lex

data class Position(var index: Int, var line: Int, var column: Int,
                    val fileName: String, val code: String) {

    fun advance(currentChar: String?) = apply {
        index++
        column++

        if (currentChar == "\n") {
            line++
            column = 0
        }
    }
}