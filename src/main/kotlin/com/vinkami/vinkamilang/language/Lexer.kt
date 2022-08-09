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
        var result = tokens

        while (i < tokens.size) {
            val currentToken = tokens[i]
            when (currentToken.type) {
                TokenType.EOF -> {
                    result = result.dropLast(tokens.size - i - 1) as MutableList<Token>
                    break
                }

                TokenType.LESS -> {
                    when (result[i+1].type) {
                        TokenType.ASSIGN -> {
                            val ctpos = currentToken.position
                            val pos = Position(ctpos.filename, ctpos.lineNumber, ctpos.start, ctpos.end + 1)
                            result[i] = Token("<=", pos)
                            result.removeAt(i+1)
                        }
                        else -> {i++}
                    }
                }

                TokenType.GREATER -> {
                    when (result[i+1].type) {
                        TokenType.ASSIGN -> {
                            val ctpos = currentToken.position
                            val pos = Position(ctpos.filename, ctpos.lineNumber, ctpos.start, ctpos.end + 1)
                            result[i] = Token(">=", pos)
                            result.removeAt(i+1)
                        }
                        else -> {i++}
                    }
                }

                TokenType.ASSIGN -> {
                    when (result[i+1].type) {
                        TokenType.ASSIGN -> {
                            val ctpos = currentToken.position
                            val pos = Position(ctpos.filename, ctpos.lineNumber, ctpos.start, ctpos.end + 1)
                            result[i] = Token("==", pos)
                            result.removeAt(i+1)
                        }
                        else -> {i++}
                    }
                }

                TokenType.PLUS -> {
                    when (result[i+1].type) {
                        TokenType.PLUS -> {
                            val ctpos = currentToken.position
                            val pos = Position(ctpos.filename, ctpos.lineNumber, ctpos.start, ctpos.end + 1)
                            result[i] = Token("++", pos)
                            result.removeAt(i+1)
                        }
                        TokenType.ASSIGN -> {
                            val ctpos = currentToken.position
                            val pos = Position(ctpos.filename, ctpos.lineNumber, ctpos.start, ctpos.end + 1)
                            result[i] = Token("+=", pos)
                            result.removeAt(i+1)
                        }
                        else -> {i++}
                    }
                }

                TokenType.MINUS -> {
                    when (result[i+1].type) {
                        TokenType.MINUS -> {
                            val ctpos = currentToken.position
                            val pos = Position(ctpos.filename, ctpos.lineNumber, ctpos.start, ctpos.end + 1)
                            result[i] = Token("--", pos)
                            result.removeAt(i+1)
                        }
                        TokenType.ASSIGN -> {
                            val ctpos = currentToken.position
                            val pos = Position(ctpos.filename, ctpos.lineNumber, ctpos.start, ctpos.end + 1)
                            result[i] = Token("-=", pos)
                            result.removeAt(i+1)
                        }
                        else -> {i++}
                    }
                }

                TokenType.MULTIPLY -> {
                    when (result[i+1].type) {
                        TokenType.ASSIGN -> {
                            val ctpos = currentToken.position
                            val pos = Position(ctpos.filename, ctpos.lineNumber, ctpos.start, ctpos.end + 1)
                            result[i] = Token("*=", pos)
                            result.removeAt(i+1)
                        }
                        TokenType.MULTIPLY -> {
                            val ctpos = currentToken.position
                            val pos = Position(ctpos.filename, ctpos.lineNumber, ctpos.start, ctpos.end + 1)
                            result[i] = Token("**", pos)
                            result.removeAt(i+1)
                        }
                        else -> {i++}
                    }
                }

                TokenType.DIVIDE -> {
                    when (result[i+1].type) {
                        TokenType.ASSIGN -> {
                            val ctpos = currentToken.position
                            val pos = Position(ctpos.filename, ctpos.lineNumber, ctpos.start, ctpos.end + 1)
                            result[i] = Token("/=", pos)
                            result.removeAt(i+1)
                        }
                        else -> {i++}
                    }
                }

                TokenType.POWER -> {
                    when (result[i+1].type) {
                        TokenType.ASSIGN -> {
                            val ctpos = currentToken.position
                            val pos = Position(ctpos.filename, ctpos.lineNumber, ctpos.start, ctpos.end + 1)
                            result[i] = Token("**=", pos)
                            result.removeAt(i+1)
                        }
                        else -> {i++}
                    }
                }

                TokenType.MODULO -> {
                    when (result[i+1].type) {
                        TokenType.ASSIGN -> {
                            val ctpos = currentToken.position
                            val pos = Position(ctpos.filename, ctpos.lineNumber, ctpos.start, ctpos.end + 1)
                            result[i] = Token("%=", pos)
                            result.removeAt(i+1)
                        }
                        else -> {i++}
                    }
                }

                TokenType.NOT -> {
                    when (result[i+1].type) {
                        TokenType.EQUAL -> {
                            val ctpos = currentToken.position
                            val pos = Position(ctpos.filename, ctpos.lineNumber, ctpos.start, ctpos.end + 1)
                            result[i] = Token("!=", pos)
                            result.removeAt(i+1)
                        }
                        else -> {i++}
                    }
                }

                TokenType.SPACE -> {
                    when (result[i+1].type) {
                        TokenType.SPACE -> {
                            val ctpos = currentToken.position
                            val pos = Position(ctpos.filename, ctpos.lineNumber, ctpos.start, ctpos.end + 1)
                            result[i] = Token(currentToken.value + result[i+1].value, pos)
                            result.removeAt(i+1)
                        }
                        else -> {i++}
                    }
                }

                TokenType.LINEBREAK -> {
                    when (result[i+1].type) {
                        TokenType.LINEBREAK -> {
                            val ctpos = currentToken.position
                            val pos = Position(ctpos.filename, ctpos.lineNumber, ctpos.start, ctpos.end + 1)
                            result[i] = Token(currentToken.value + result[i+1].value, pos)
                            result.removeAt(i+1)
                        }
                        else -> {i++}
                    }
                }

                else -> {i++}
            }
        }

        return result
    }
}