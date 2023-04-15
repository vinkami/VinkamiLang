package com.vinkami.vinkamilang.language.exception

import com.vinkami.vinkamilang.language.lex.Position

@Suppress("unused")
class DebugMessage(message: String, startPosition: Position, endPosition: Position): BaseError(message, startPosition, endPosition)