package com.vinkami.vinkamilang.language.exception

import com.vinkami.vinkamilang.language.lex.Position

class IllegalCharError(value: String, startPos: Position, endPos: Position): BaseError(value, startPos, endPos)