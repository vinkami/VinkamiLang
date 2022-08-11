package com.vinkami.vinkamilang.language

enum class TokenType {
    // Key char
    L_PARAN, R_PARAN, L_BRAC, R_BRAC, L_BRACE, R_BRACE,
    COMMA, DOT, COLON,

    // Operator
    PLUS, MINUS, MULTIPLY, DIVIDE, POWER, MODULO,
    EQUAL, LESS, LESS_EQUAL, GREATER, GREATER_EQUAL, NOT_EQUAL,
    ASSIGN, INCREMENT, DECREMENT,
    PLUS_ASSIGN, MINUS_ASSIGN, MULTIPLY_ASSIGN, DIVIDE_ASSIGN, POWER_ASSIGN, MODULO_ASSIGN,

    // LOGIC
    AND, OR, NOT,

    // Keyword
    IF, ELIF, ELSE, TRUE, FALSE, FOR, WHILE, RETURN, VAR, IS, IMPORT, IN, COMPLETE, INCOMPLETE, BREAK,

    // Literal
    IDENTIFIER, NUMBER, UNKNOWN, STRING,

    // Format
    SPACE, LINEBREAK, EOF,

    ;

    override fun toString(): String {
        return this.name
    }
}