package com.vinkami.vinkamilang.language.exception

import com.vinkami.vinkamilang.language.lex.Position

class DebugMessage(message: String, startPosition: Position, endPosition: Position): BaseLangException(message, startPosition, endPosition)