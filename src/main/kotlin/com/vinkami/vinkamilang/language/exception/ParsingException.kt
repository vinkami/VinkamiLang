package com.vinkami.vinkamilang.language.exception

import com.vinkami.vinkamilang.language.position.LexingPosition

class ParsingException(message: String, position: LexingPosition): BaseLangException(message, position)