package com.vinkami.vinkamilang.language.expression

class If(val condition: Expression, val action: Expression): Expression(condition) {
  override fun toString(): String {
    return "(if ($condition) {$action})"
  }
}