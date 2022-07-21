package com.vinkami.vinkamilang.language

enum class TokenType {
    // Key char
    L_PARAN, R_PARAN, L_BRAC, R_BRAC,
    COMMA, DOT, COLON, EXCLAM,

    // Operator
    PLUS, MINUS, MULTIPLY, DIVIDE, POWER, MODULO,
    EQUAL, LESS_THAN, LESS_EQUAL, GREATER_THAN, GREATER_EQUAL, NOT_EQUAL, AND, OR,

    // Keyword
    IF, ELIF, ELSE, TRUE, FALSE, FOR, WHILE, RETURN, VAR, IS, IMPORT, IN,

    // Literal
    IDENTIFIER, INT, FLOAT, STRING,

    // Format
    SPACE, TAB, LINEBREAK, EOF,
}