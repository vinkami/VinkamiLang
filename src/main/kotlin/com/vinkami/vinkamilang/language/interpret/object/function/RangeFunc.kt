package com.vinkami.vinkamilang.language.interpret.`object`.function

import com.vinkami.vinkamilang.language.exception.TypeError
import com.vinkami.vinkamilang.language.interpret.Referables
import com.vinkami.vinkamilang.language.interpret.`object`.BaseObject
import com.vinkami.vinkamilang.language.interpret.`object`.ListObj
import com.vinkami.vinkamilang.language.interpret.`object`.NullObj
import com.vinkami.vinkamilang.language.interpret.`object`.NumberObj
import com.vinkami.vinkamilang.language.lex.Position

class RangeFunc: BuiltinFunc("range") {
    // start: starting number; required
    // end: ending number; optional; defaults to null
    // step: step size; optional; defaults to 1
    // returns: list of numbers from start (inclusive) to end (exclusive) with step size step;
    //          if end is null, returns list of numbers from 0 to start (exclusive) with step size step

    override val parameters = listOf(
        Parameter("start", "Number", null),
        Parameter("end", "Number", NullObj(startPos, endPos)),
        Parameter("step", "Number", NumberObj(1f, startPos, endPos), true)
    )

    override operator fun invoke(ref: Referables, startPos: Position, endPos: Position): BaseObject {
        val start = ref.get("start")
        val end = ref.get("end")
        val step = ref.get("step")

        if (start !is NumberObj) throw TypeError("Expected Number for start", startPos, endPos)
        if (step !is NumberObj) throw TypeError("Expected Number for step", startPos, endPos)

        when (end) {
            is NullObj -> {
                val list = mutableListOf<NumberObj>()
                var i = 0f
                while (i < start.value) {
                    list.add(NumberObj(i, startPos, endPos))
                    i += step.value
                }
                return ListObj(list, startPos, endPos)

            }

            is NumberObj -> {
                val list = mutableListOf<NumberObj>()
                var i = start.value
                while (i < end.value) {
                    list.add(NumberObj(i, startPos, endPos))
                    i += step.value
                }
                return ListObj(list, startPos, endPos)
            }

            else -> throw TypeError("Expected Number or null for end", startPos, endPos)
        }
    }
}