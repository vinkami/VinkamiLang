package com.vinkami.vinkamilang

import com.vinkami.vinkamilang.language.Script
import org.bukkit.plugin.java.JavaPlugin

class PathFinder(var plugin: JavaPlugin) {
    var scripts = mutableListOf<Script>()
    val cmd = Commands(this)


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
}