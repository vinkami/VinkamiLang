package com.vinkami.vinkamilang.language.interpret

import com.vinkami.vinkamilang.language.lex.Position

class Logger {
    val logs = mutableListOf<Log>()

    class Log(val level: Int, val message: String, val startPos: Position, val endPos: Position) {
        override fun toString(): String {
            return "[$level] $message"
        }
    }

    fun log(level: Int, message: String, startPos: Position, endPos: Position) {
        logs.add(Log(level, message, startPos, endPos))
    }

    fun trace(message: String, startPos: Position, endPos: Position) {
        logs.add(Log(0, message, startPos, endPos))
    }

    fun debug(message: String, startPos: Position, endPos: Position) {
        logs.add(Log(1, message, startPos, endPos))
    }

    fun info(message: String, startPos: Position, endPos: Position) {
        logs.add(Log(2, message, startPos, endPos))
    }
}