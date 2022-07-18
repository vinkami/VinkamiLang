package com.vinkami.vinkamilang

import com.vinkami.vinkamilang.command.Commands
import com.vinkami.vinkamilang.script.Script
import org.bukkit.plugin.java.JavaPlugin

class PathFinder(var plugin: JavaPlugin) {
    var scripts = mutableListOf<Script>()
    val event = Events(this)
    val cmd = Commands(this)

    init {
        this.loadScripts()
    }

    private fun loadScripts() {
        for (file in this.plugin.dataFolder.resolve("scripts").listFiles()!!) {
            scripts.add(Script(file, this))
        }
    }

    fun reloadScripts() {
        this.scripts.clear()
        this.loadScripts()
    }
}