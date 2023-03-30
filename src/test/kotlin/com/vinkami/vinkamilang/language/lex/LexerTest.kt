package com.vinkami.vinkamilang.language.lex

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LexerTest {
    private val fileName = "<test>"

    private fun compareTokens(expected: Token, actual: Token, message: String? = null) {
        assertEquals(expected.type, actual.type, message)
        assertEquals(expected.value, actual.value, message)
        assertEquals(expected.startPos, actual.startPos, message)
        assertEquals(expected.endPos, actual.endPos, message)
    }

    private fun compareTokens(expected: List<Token>, actual: List<Token>, message: String? = null) {
        assertEquals(expected.size, actual.size)
        for (i in expected.indices) {
            compareTokens(expected[i], actual[i], message)
        }
    }

    @Test
    fun string() {
        val text = "\"Hello world!\""
        val actual: List<Token> = Lexer(text, fileName).tokenize()
        val expected = listOf(
            Token(TokenType.STRING, "Hello world!", Position(0, 0, 0, fileName, text), Position(14, 0, 14, fileName, text)),
            Token(TokenType.EOF, "EOF", Position(15, 0, 15, fileName, text), Position(15, 0, 15, fileName, text))
        )

        compareTokens(expected, actual, "String tokenization failed")
    }

    @Test
    fun number() {
        val text = "12.3"
        val actual: List<Token> = Lexer(text, fileName).tokenize()
        val expected = listOf(
            Token(TokenType.NUMBER, "12.3", Position(0, 0, 0, fileName, text), Position(4, 0, 4, fileName, text)),
            Token(TokenType.EOF, "EOF", Position(4, 0, 4, fileName, text), Position(5, 0, 5, fileName, text))
        )

        compareTokens(expected[0], actual[0], "Number tokenization failed")
    }
}