package com.vinkami.vinkamilang

import com.vinkami.vinkamilang.language.Script
import org.bukkit.plugin.java.JavaPlugin

class PathFinder(var plugin: JavaPlugin) {
    var scripts = mutableListOf<Script>()
    val cmd = Commands(this)
    val cmdtc = CommandTabCompleter(this)
    @Suppress("unused")
    val logger = plugin.logger
    val scriptFolder = plugin.dataFolder.resolve("scripts")
    val config = plugin.config

    init {
        this.loadScripts()
        Events(this)
    }

    /**
     * Load all scripts from the script folder
     * @return `true` if any `main.vk` file can be found and loaded, `false` otherwise
     */
    private fun loadScripts(): Boolean {
        if (!scriptFolder.exists()) scriptFolder.mkdirs()

        for (file in scriptFolder.listFiles()!!) {
            if (file.isFile && file.extension == "vk" && !file.name.startsWith("-")) {
                scripts.add(Script(file, this))
            }
        }

        scripts.forEach { script -> if (script.name == config.getString("startup script")) return true }
        return false
    }

    fun reloadScripts(): Boolean {
        scripts.clear()
        return loadScripts()
    }

    fun listScripts(): List<String> {
        return scripts.map { it.name }
    }
}