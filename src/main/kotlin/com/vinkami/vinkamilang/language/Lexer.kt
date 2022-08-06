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

//    private fun rollback() {
//        this.pos--
//        this.current_char = this.text[this.pos].toString()
//    }

    fun tokenize(): List<Token> {
        val tokens: MutableList<Token> = mutableListOf()

        while (this.current_char != null) {
            val section =
                if (Regex("[0-9.]") matches this.current_char!!) {this.makeNumber()}
                else if (Regex("[a-zA-Z]") matches this.current_char!!) {this.makeIdentifier()}
                else if (Regex("[\\s\\t\\n\\r]") matches this.current_char!!) {this.makeWhitespace()}
                else if (Regex("[\'\"]") matches this.current_char!!) {this.makeString()}
                else {
                    val c = this.current_char!!
                    this.advance()
                    c
                }

            tokens += Token(section)
        }

        // EOF token
        tokens += Token("EOF")

        return tokens
    }

    private fun makeNumber(): String {  // Note: Doesn't care the decimal points' number and placement
        var section = ""

        while (this.current_char != null && Regex("[0-9.]") matches this.current_char!!) {
            section += this.current_char
            this.advance()
        }

        return section
    }

    private fun makeIdentifier(): String {
        var section = ""

        while (this.current_char != null && Regex("[a-zA-Z]") matches this.current_char!!) {
            section += this.current_char
            this.advance()
        }

        return section
    }

    private fun makeWhitespace(): String {
        var section = ""

        while (this.current_char != null && Regex("[\\s\\t\\n\\r]") matches this.current_char.toString()) {
            section += this.current_char
            this.advance()
        }

        return section
    }

    private fun makeString(): String {
        var section = "${this.current_char}"
        val quote = this.current_char!!
        this.advance()

        while (this.current_char != null && (this.current_char != quote || if (this.pos != 0) {this.text[this.pos - 1].toString() == "\\"} else true)) {
            section += this.current_char
            this.advance()
        }

        if (this.current_char != null) {
            section += quote
            this.advance()  // Current char is the quote, need to advance 1 more time
        }

        return section
    }
}