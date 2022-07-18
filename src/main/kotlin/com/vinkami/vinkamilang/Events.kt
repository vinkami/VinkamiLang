package com.vinkami.vinkamilang

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerJoinEvent

class Events(private val pf: PathFinder) : Listener {
    init {
        this.pf.plugin.server.pluginManager.registerEvents(this, this.pf.plugin)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.player.sendMessage("Hi!")
    }

    @EventHandler
    fun onCommand(event: PlayerCommandPreprocessEvent) {
        val cmd = event.message
        val player = event.player
        val (worked: Boolean, result: String) = this.pf.cmd.execute(player, cmd)
        if (worked) {
            event.isCancelled = true
            if (result.isNotBlank()) {
                player.sendMessage(result)
            }
        }
    }
}