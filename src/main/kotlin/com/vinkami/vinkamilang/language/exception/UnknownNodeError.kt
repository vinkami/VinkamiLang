package com.vinkami.vinkamilang.language.exception

import com.vinkami.vinkamilang.language.parse.node.BaseNode

class UnknownNodeError(node: BaseNode): BaseLangException(node::class.simpleName.toString(), node.startPos, node.endPos)