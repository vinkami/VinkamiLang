package com.vinkami.vinkamilang.command

import com.vinkami.vinkamilang.PathFinder
import com.vinkami.vinkamilang.language.Lexer
import com.vinkami.vinkamilang.language.Parser
import com.vinkami.vinkamilang.language.exception.LexingException
import com.vinkami.vinkamilang.language.exception.ParsingException
import org.bukkit.entity.Player

@Suppress("UNUSED_PARAMETER")
class Commands(private val pf: PathFinder) {
    fun execute(sender: Player, cmd: String): Pair<Boolean, String> {
        val things: List<String> = cmd.split(" ")

        return when (things[0]) {
            "/fly" -> true to this.fly(sender, things.drop(1))
            "/vk" -> true to this.vk(sender, things.drop(1))

            else -> false to ""  // Not a command from here
        }
    }

    private fun fly(sender: Player, args: List<String>): String {
        if (args.isNotEmpty()) {return "No arguments for /fly"}

        return if (sender.allowFlight) {
            sender.allowFlight = false
            "Flying is disabled"
        } else {
            sender.allowFlight = true
            "Flying is enabled"
        }
    }

    private fun vk(sender: Player, args: List<String>): String {
        if (args.isEmpty()) {return "Usage: /vk [reload|scripts|print|run]"}

        return when (args[0]) {
            "reload" -> {
                this.pf.reloadScripts()
                "Scripts reloaded"
            }

            "scripts" -> this.pf.scripts.toString()

            "print" -> {
                val scriptName = args.drop(1).joinToString(" ")
                val script = this.pf.scripts.find { it.name == scriptName } ?: return "Script not found"
                script.code
            }

            "run" -> {
                val scriptName = args.drop(1).joinToString(" ")
                val script = this.pf.scripts.find { it.name == scriptName } ?: return "Script not found"

                try {
                    val tokens = Lexer(script.code, script.name).tokenize()
                    Parser(tokens).parse().toString()
                } catch (e: LexingException) {
                    e.toString()
                } catch (e: ParsingException) {
                    e.toString()
                }
            }

            "runlex" -> {
                val scriptName = args.drop(1).joinToString(" ")
                val script = this.pf.scripts.find { it.name == scriptName } ?: return "Script not found"

                try {
                    val tokens = Lexer(script.code, script.name).tokenize()
                    tokens.toString()
                } catch (e: LexingException) {
                    e.toString()
                }
            }

            else -> "Unknown usage"
        }
    }
}