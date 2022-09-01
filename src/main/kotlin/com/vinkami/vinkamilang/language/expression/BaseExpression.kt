package com.vinkami.vinkamilang.language.expression

import com.vinkami.vinkamilang.language.position.ParsingPosition
import com.vinkami.vinkamilang.language.statement.BaseStatement

abstract class BaseExpression(position: ParsingPosition): BaseStatement(position)