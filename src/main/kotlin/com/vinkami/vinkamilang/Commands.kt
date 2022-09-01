package com.vinkami.vinkamilang

import com.vinkami.vinkamilang.language.Interpreter
import com.vinkami.vinkamilang.language.Lexer
import com.vinkami.vinkamilang.language.Parser
import com.vinkami.vinkamilang.language.exception.LexingException
import com.vinkami.vinkamilang.language.exception.ParsingException
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class Commands(private val pf: PathFinder): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) { return true }

        if (command.name.lowercase() == "vk") {
            when (args[0].lowercase()) {
                "reload" -> vkreload(sender)
                "scripts" -> vkscripts(sender)
                "print" -> vkprint(sender, args[1])
                "run" -> vkrun(sender, args[1])
                "runlex" -> vkrunlex(sender, args[1])
                "runparse" -> vkrunparse(sender, args[1])

                "help", "?" -> vkhelp(sender)
                else -> vkhelp(sender)
            }
        }

        return true
    }

    private fun vkhelp(player: CommandSender) {
        val helps = mapOf(
            "/vk reload" to "Reload the scripts",
            "/vk scripts" to "List the scripts",
            "/vk print <script>" to "Print the script",
            "/vk run <script>" to "Run the script",
            "/vk runlex <script>" to "Run the script with lexer only",
            "/vk runparse <script>" to "Run the script with lexer and parser only",
        )

        for ((k, v) in helps) {
            player.sendMessage("$k - $v")
        }
    }

    private fun vkreload(player: CommandSender) {
        pf.reloadScripts()
        player.sendMessage("Scripts reloaded")
    }

    private fun vkscripts(player: CommandSender) {
        player.sendMessage(pf.scripts.toString())
    }

    private fun vkprint(player: CommandSender, scriptName: String) {
        val script = this.pf.scripts.find { it.name == scriptName }

        if (script == null) {
            player.sendMessage("Script not found")
            return
        }

        player.sendMessage(script.code)
    }

    private fun vkrun(player: CommandSender, scriptName: String) {
        val script = this.pf.scripts.find { it.name == scriptName }

        if (script == null) {
            player.sendMessage("Script not found")
            return
        }

        player.sendMessage(
            try {
                val tokens = Lexer(script.code, script.name).tokenize()
                val cst = Parser(tokens).parse()
                Interpreter(cst).interpret()
            } catch (e: LexingException) {
                e.toString()
            } catch (e: ParsingException) {
                e.toString()
            }
        )
    }

    private fun vkrunlex(player: CommandSender, scriptName: String) {
        val script = this.pf.scripts.find { it.name == scriptName }

        if (script == null) {
            player.sendMessage("Script not found")
            return
        }

        player.sendMessage(
            try {
                Lexer(script.code, script.name).tokenize().toString()
            } catch (e: LexingException) {
                e.toString()
            }
        )
    }

    private fun vkrunparse(player: CommandSender, scriptName: String) {
        val script = this.pf.scripts.find { it.name == scriptName }

        if (script == null) {
            player.sendMessage("Script not found")
            return
        }

        player.sendMessage(
            try {
                Parser(Lexer(script.code, script.name).tokenize()).parse().toString()
            } catch (e: LexingException) {
                e.toString()
            } catch (e: ParsingException) {
                e.toString()
            }
        )
    }
}
