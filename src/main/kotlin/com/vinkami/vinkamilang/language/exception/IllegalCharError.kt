package com.vinkami.vinkamilang.language.exception

import com.vinkami.vinkamilang.language.lex.Token

class IllegalCharError(token: Token): BaseLangException(token.value, token.startPos, token.endPos)