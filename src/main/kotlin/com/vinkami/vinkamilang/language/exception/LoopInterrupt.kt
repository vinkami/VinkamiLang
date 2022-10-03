package com.vinkami.vinkamilang.language.exception

import com.vinkami.vinkamilang.language.lex.Position

class LoopInterrupt(startPos: Position, endPos: Position): BaseError("Loop interrupted", startPos, endPos)