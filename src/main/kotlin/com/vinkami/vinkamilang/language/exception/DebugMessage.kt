package com.vinkami.vinkamilang.language.exception

import com.vinkami.vinkamilang.language.lex.Position

class DebugMessage(message: String, pos: Position): BaseLangException(message, pos, pos)