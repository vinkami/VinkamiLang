package com.vinkami.vinkamilang.language.exception

import com.vinkami.vinkamilang.language.lex.Position

class TypeError(message: String, startPos: Position, endPos: Position): BaseError(message, startPos, endPos)