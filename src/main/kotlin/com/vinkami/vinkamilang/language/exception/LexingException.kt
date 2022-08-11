package com.vinkami.vinkamilang.language.exception

import com.vinkami.vinkamilang.language.position.LexingPosition

class LexingException(message: String, position: LexingPosition): BaseLangException(message, position)