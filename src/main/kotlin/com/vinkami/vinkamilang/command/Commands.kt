package com.vinkami.vinkamilang.command

import com.vinkami.vinkamilang.PathFinder
import com.vinkami.vinkamilang.language.Lexer
import org.bukkit.entity.Player

@Suppress("UNUSED_PARAMETER")
class Commands(val pf: PathFinder) {
    fun execute(sender: Player, cmd: String): Pair<Boolean, String> {
        val things: List<String> = cmd.split(" ")

        return when (things[0]) {
            "/fly" -> this.fly(sender, things.drop(1))
            "/vk" -> this.vk(sender, things.drop(1))

            else -> false to ""  // Not a command from here
        }
    }

    private fun fly(sender: Player, args: List<String>): Pair<Boolean, String> {
        if (args.isNotEmpty()) {return true to "No arguments for /fly"}

        return if (sender.allowFlight) {
            sender.allowFlight = false
            true to "Flying is disabled"
        } else {
            sender.allowFlight = true
            true to "Flying is enabled"
        }
    }

    private fun vk(sender: Player, args: List<String>): Pair<Boolean, String> {
        if (args.isEmpty()) {return true to "Usage: /vk [reload|scripts]"}

        return when (args[0]) {
            "reload" -> {
                this.pf.reloadScripts()
                true to "Scripts reloaded"
            }

            "scripts" -> true to this.pf.scripts.toString()

            "run" -> {
                val cmd = args.drop(1).joinToString(" ")
                true to Lexer(cmd).tokenize().toString()
            }

            else -> true to "Unknown usage"
        }
    }
}