package com.vinkami.vinkamilang

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class CommandTabCompleter(private val pf: PathFinder): TabCompleter {
    private val commands = listOf("reload", "scripts", "print", "run", "runlex", "runparse", "execute", "help", "?")

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): List<String>? {
        val actualArgs = args.filter { it.isNotEmpty() }
        if (actualArgs.isEmpty()) return commands.toList()

        return if (actualArgs.size == 2 || (actualArgs.size == 1 && args.last().isEmpty())) subargOf(actualArgs)
        else if (actualArgs.size == 1) commands.filter { it.startsWith(actualArgs[0]) }
        else null
    }

    private fun subargOf(args: List<String>) = when (args[0]) {
        "print", "run", "runlex", "runparse" -> pf.listScripts().filter { it.startsWith(args[1]) }
        "execute" -> listOf("\"Lorem ipsum\"")
        else -> null
    }
}