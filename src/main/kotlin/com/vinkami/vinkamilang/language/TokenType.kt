package com.vinkami.vinkamilang.language

enum class TokenType {
    // Key char
    L_PARAN, R_PARAN, L_BRAC, R_BRAC,
    COMMA, DOT, COLON,

    // Operator
    PLUS, MINUS, MULTIPLY, DIVIDE, POWER, MODULO,
    EQUAL, LESS, LESS_EQUAL, GREATER, GREATER_EQUAL, NOT_EQUAL,

    // LOGIC
    AND, OR, NOT,

    // Keyword
    IF, ELIF, ELSE, TRUE, FALSE, FOR, WHILE, RETURN, VAR, IS, IMPORT, IN,

    // Literal
    IDENTIFIER, NUMBER, UNKNOWN, STRING,

    // Format
    SPACE, LINEBREAK, EOF,
}