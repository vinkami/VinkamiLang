package com.vinkami.vinkamilang.language.exception

import com.vinkami.vinkamilang.language.Position

class LexingException(message: String, position: Position): BaseLangException(message, position)