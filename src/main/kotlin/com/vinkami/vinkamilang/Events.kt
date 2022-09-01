package com.vinkami.vinkamilang

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class Events(pf: PathFinder) : Listener {
    init {
        pf.plugin.server.pluginManager.registerEvents(this, pf.plugin)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.player.sendMessage("Hi!")
    }
}