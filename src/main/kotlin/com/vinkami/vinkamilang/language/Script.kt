package com.vinkami.vinkamilang.language

import com.vinkami.vinkamilang.PathFinder
import com.vinkami.vinkamilang.language.exception.BaseError
import com.vinkami.vinkamilang.language.interpret.Interpreter
import com.vinkami.vinkamilang.language.interpret.Referables
import com.vinkami.vinkamilang.language.lex.Lexer
import com.vinkami.vinkamilang.language.lex.Token
import com.vinkami.vinkamilang.language.parse.Parser
import com.vinkami.vinkamilang.language.parse.node.BaseNode
import java.io.File

class Script {
    val code: String
    val name: String
    val hasError get() = ::error.isInitialized

    lateinit var tokens: List<Token>
    lateinit var node: BaseNode
    lateinit var error: BaseError

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

    fun run(ref: Referables): String? {
        lex()
        parse()
        if (hasError) return error.toString()
        if (!::node.isInitialized) return "NotYourFaultError: No node found"
        interpret(ref)
        if (hasError) return error.toString()
        return null
    }

    fun lex() = apply {
        tokens = Lexer(code, name).tokenize()
    }

    fun parse() = apply {
        val result = Parser(tokens).parse()
        if (result.hasError) this.error = result.error
        if (result.hasNode) this.node = result.node
    }

    fun interpret(ref: Referables) = apply {
        val result = Interpreter(node, ref).interpret()
        if (result.hasError) this.error = result.error
    }
}
