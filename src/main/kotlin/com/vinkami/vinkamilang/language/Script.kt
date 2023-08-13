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

    fun lex(): Pair<List<Token>?, BaseError?> {
        return try {
            Lexer(code, name).tokenize() to null
        } catch (e: BaseError) {
            null to e
        }
    }

    fun parse(): Pair<BaseNode?, BaseError?> {
        try {
            val tokens = lex().also { if (it.second != null) return null to it.second }.first!!
            return Parser(tokens).parse() to null
        } catch (e: BaseError) {
            return null to e
        }
    }

    fun interpret(ref: Referables): BaseError? {
        val node = parse().also { if (it.second != null) return it.second }.first!!
        return Interpreter(node, ref).interpret()
    }
}
