package com.vinkami.vinkamilang.language.exception

import com.vinkami.vinkamilang.language.lex.Token

class IllegalCharError(token: Token): BaseError(token.value, token.startPos, token.endPos)