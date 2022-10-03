package com.vinkami.vinkamilang.language.exception

import com.vinkami.vinkamilang.language.lex.Position

abstract class BaseError(message: String, startPos: Position, endPos: Position): BaseLangException(message, startPos, endPos)