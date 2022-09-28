package com.vinkami.vinkamilang

import com.vinkami.vinkamilang.language.Script
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class Commands(private val pf: PathFinder): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) { return true }
        if (command.name.lowercase() != "vk") { return true }

        when (args[0].lowercase()) {
            "reload" -> vkreload(sender)
            "scripts" -> vkscripts(sender)
            "print" -> vkprint(sender, args.getOrElse(1) { "main" })
            "run" -> vkrun(sender, args.getOrElse(1) { "main" })
            "runlex" -> vkrunlex(sender, args.getOrElse(1) { "main" })
            "runparse" -> vkrunparse(sender, args.getOrElse(1) { "main" })
            "execute" -> vkexecute(sender, args.drop(1))

            "help", "?" -> vkhelp(sender)
            else -> vkhelp(sender)
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
            "/vk execute <code>" to "Execute the code in real time",
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

        player.sendMessage(script.run())
    }

    private fun vkrunlex(player: CommandSender, scriptName: String) {
        val script = this.pf.scripts.find { it.name == scriptName }

        if (script == null) {
            player.sendMessage("Script not found")
            return
        }

        player.sendMessage(script.lex().tokens.toString())
    }

    private fun vkrunparse(player: CommandSender, scriptName: String) {
        val script = this.pf.scripts.find { it.name == scriptName }

        if (script == null) {
            player.sendMessage("Script not found")
            return
        }

        script.lex().parse()

        player.sendMessage(
            if (script.hasError) {
                script.error.toString()
            } else {
                script.node.toString()
            }
        )
    }

    private fun vkexecute(player: CommandSender, codes: List<String>) {
        val code = codes.joinToString(" ")
        val script = Script("<stdin>", code)
        player.sendMessage(script.run())
    }
}
