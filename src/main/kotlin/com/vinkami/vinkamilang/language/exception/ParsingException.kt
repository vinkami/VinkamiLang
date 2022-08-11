package com.vinkami.vinkamilang.language.exception

import com.vinkami.vinkamilang.language.Position

class ParsingException(message: String, position: Position): BaseLangException(message, position)