package com.vinkami.vinkamilang.language

import com.vinkami.vinkamilang.PathFinder
import com.vinkami.vinkamilang.language.exception.BaseError
import com.vinkami.vinkamilang.language.interpret.Interpreter
import com.vinkami.vinkamilang.language.interpret.Referables
import com.vinkami.vinkamilang.language.interpret.`object`.BaseObject
import com.vinkami.vinkamilang.language.lex.Lexer
import com.vinkami.vinkamilang.language.lex.Token
import com.vinkami.vinkamilang.language.parse.Parser
import com.vinkami.vinkamilang.language.parse.node.BaseNode
import java.io.File

class Script {
    val code: String
    val name: String

    var error: BaseError? = null

    constructor(file: File, pf: PathFinder) {
        this.name = file.relativeTo(pf.scriptFolder)
            .toString()
            .replace("\\", ".")
            .replace(".vk", "")
        code = file.readText()
    }

    constructor(name: String, code: String) {
        this.name = name
        this.code = code
    }

    override fun toString(): String {
        return "<Script $name>"
    }

    fun lex(): List<Token>? {
        try {
            return Lexer(code, name).tokenize()
        } catch (e: BaseError) {
            error = e
        }
        return null
    }

    fun parse(): BaseNode? {
        try {
            val tokens = lex() ?: return null
            return Parser(tokens).parse()
        } catch (e: BaseError) {
            error = e
        }
        return null
    }

    fun interpret(ref: Referables): BaseObject? {
        val node = parse() ?: return null
        val result = Interpreter(node, ref).interpret()
        if (result.hasError) {
            error = result.error
            return error
        }
        return null
    }
}
