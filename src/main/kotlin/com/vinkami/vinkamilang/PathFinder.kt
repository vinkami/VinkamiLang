package com.vinkami.vinkamilang

import com.vinkami.vinkamilang.language.Script
import org.bukkit.plugin.java.JavaPlugin

class PathFinder(var plugin: JavaPlugin) {
    var scripts = mutableListOf<Script>()
    val cmd = Commands(this)
    val cmdtc = CommandTabCompleter(this)
    @Suppress("unused")
    val logger = plugin.logger

    init {
        this.loadScripts()
        Events(this)
    }

    private fun loadScripts() {
        for (file in plugin.dataFolder.resolve("scripts").listFiles()!!) {
            scripts.add(Script(file, this))
        }
    }

    fun reloadScripts() {
        scripts.clear()
        loadScripts()
    }

    fun listScripts(): List<String> {
        return scripts.map { it.name }
    }
}