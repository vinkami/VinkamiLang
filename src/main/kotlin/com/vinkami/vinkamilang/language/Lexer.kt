package com.vinkami.vinkamilang.language

class Lexer(val text: String) {
    var pos = -1
    var current_char: String? = null
    init {
        this.advance()
    }

    private fun advance() {
        this.pos++
        this.current_char = if (this.pos < this.text.length) this.text[this.pos].toString() else null
    }

    private fun rollback() {
        this.pos--
        this.current_char = this.text[this.pos].toString()
    }

    private fun tokenize(): List<Token> {
        val tokens: MutableList<Token> = mutableListOf()

        while (this.current_char != null) {
            val section =
                if (Regex("\\d") matches this.current_char!!) {this.makeNumber()}
                else if (Regex("[a-zA-Z]") matches this.current_char!!) {TODO("Identifier vs String")}
                else {this.current_char!!}

            tokens += Token(section)
        }

        return tokens
    }

    private fun makeNumber(): String {
        var section = ""
        var dotCount = 0

        while (Regex("\\d") matches this.current_char.toString()) {
            section += if (this.current_char == ".") {
                if (dotCount == 1) {break}
                dotCount++
                "."
            } else {
                this.current_char
            }
            this.advance()
        }
        this.rollback()

        return section
    }
}