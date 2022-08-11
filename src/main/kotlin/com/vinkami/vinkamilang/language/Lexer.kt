package com.vinkami.vinkamilang.language

class Lexer(private val text: String, private val fileName: String) {
    private var pos = -1
    private val currentChar: String?
        get() = if (pos < text.length) text[pos].toString() else null
    private var lineNumber: Int = 1

    init {advance()}

    private fun advance() {
        pos++
        if (currentChar == "\n") {
            lineNumber++
        }
    }

    fun tokenize(): List<Token> {
        val tokens: MutableList<Token> = mutableListOf()

        while (currentChar != null) {
            val startPos = pos

            val section =
                if (Regex("[0-9.]") matches currentChar!!) {makeNumber()}
                else if (Regex("[a-zA-Z]") matches currentChar!!) {makeIdentifier()}
                else if (Regex("[\'\"]") matches currentChar!!) {makeString()}
                else {
                    val c = currentChar!!
                    advance()
                    c
                }

            val position = Position(fileName, lineNumber, startPos, pos)
            tokens += Token(section, position)
        }

        // EOF token
        advance()
        val position = Position(fileName, lineNumber, pos, pos)
        tokens += Token("EOF", position)

        return combineTokens(tokens)  // Combine tokens like >= and ++
    }

    private fun makeNumber(): String {  // Note: Doesn't care the decimal points' number and placement
        var section = ""

        while (currentChar != null && Regex("[0-9.]") matches currentChar!!) {
            section += currentChar
            advance()
        }

        return section
    }

    private fun makeIdentifier(): String {
        var section = ""

        while (currentChar != null && Regex("[a-zA-Z]") matches currentChar!!) {
            section += currentChar
            advance()
        }

        return section
    }

    private fun makeString(): String {
        var section = "${currentChar}"
        val quote = currentChar!!
        advance()

        while (currentChar != null && (currentChar != quote || if (pos != 0) {text[pos - 1].toString() == "\\"} else true)) {
            section += currentChar
            advance()
        }

        if (currentChar != null) {
            section += quote
            advance()  // Current char is the quote, need to advance 1 more time
        }

        return section
    }

    private fun combineTokens(tokens: MutableList<Token>): MutableList<Token> {
        // For tokens like >=, ++, etc. they are not combined in the first lexing step, here will do it.
        // Working principle: loop all tokens, replace it and the next token if combinable, increment i otherwise

        var i = 0

        while (i < tokens.size) {
            val currentToken = tokens[i]
            if (currentToken.type == TokenType.EOF) {
                return tokens.dropLast(tokens.size - i - 1) as MutableList<Token>
            }
            val nextToken = tokens[i + 1]

            if (Constant.conbinableTokens.contains(currentToken.type to nextToken.type)) {
                val procedure = Constant.conbinableTokens[currentToken.type to nextToken.type]!!

                val newTT = procedure.first
                val newValue = procedure.second(currentToken, nextToken)

                val ctpos = currentToken.position
                val pos = Position(ctpos.filename, ctpos.lineNumber, ctpos.start, ctpos.end + 1)
                tokens[i] = Token(newTT, newValue, pos)
                tokens.removeAt(i + 1)
            } else { i++ }  // Some tokens can combine multiple times, so don't increment if a combination happens

        }

        return tokens
    }
}