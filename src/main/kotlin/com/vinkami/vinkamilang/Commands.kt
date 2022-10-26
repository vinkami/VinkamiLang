package com.vinkami.vinkamilang

import com.vinkami.vinkamilang.language.Script
import com.vinkami.vinkamilang.language.interpret.Referables
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class Commands(private val pf: PathFinder): CommandExecutor {
    private val vkexecuteRef = Referables()
    private val commandList = mapOf(
        // name - (usage - description) - {method}
        "reload" to Triple("/vk reload", "Reload the scripts") {sender: CommandSender, _: Array<out String> -> vkreload(sender)},
        "scripts" to Triple("/vk scripts", "List the scripts") {sender: CommandSender, _: Array<out String> -> vkscripts(sender)},
        "print" to Triple("/vk print <script>", "Print the script") {sender: CommandSender, args: Array<out String> -> vkprint(sender, args.getOrElse(1) { "main" })},
        "run" to Triple("/vk run <script>", "Run the script") {sender: CommandSender, args: Array<out String> -> vkrun(sender, args.getOrElse(1) { "main" })},
        "runlex" to Triple("/vk runlex <script>", "Run the script with lexer only") {sender: CommandSender, args: Array<out String> -> vkrunlex(sender, args.getOrElse(1) { "main" })},
        "runparse" to Triple("/vk runparse <script>", "Run the script with lexer and parser only") {sender: CommandSender, args: Array<out String> -> vkrunparse(sender, args.getOrElse(1) { "main" })},
        "execute" to Triple("/vk execute <code>", "Execute the code in real time") {sender: CommandSender, args: Array<out String> -> vkexecute(sender, args.drop(1))},
    )

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) { return true }
        if (command.name.lowercase() != "vk") { return true }
        if (args.isEmpty()) { vkhelp(sender); return true }

        else {
            val actualArgs = args.filter { it.isNotEmpty() }
            val cmd = commandList[actualArgs[0]]
            if (actualArgs[0] in listOf("help", "?")) {
                vkhelp(sender)
            } else if (cmd == null) {
                sender.sendMessage("Unknown command: ${actualArgs[0]}")
                vkhelp(sender)
            } else {
                cmd.third(sender, actualArgs.toTypedArray())
            }
        }

        return true
    }


    private fun vkhelp(player: CommandSender) {
        player.sendMessage("VinkamiLang commands:")
        for ((_, trio) in commandList) {
            val (usage, desc, _) = trio
            player.sendMessage("$usage - $desc")
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
        player.sendMessage(script.run(vkexecuteRef))
    }
}
