package com.vinkami.vinkamilang

import org.bukkit.plugin.java.JavaPlugin

class VinkamiLang : JavaPlugin() {
    override fun onEnable() {
        server.pluginManager.registerEvents(Events(), this)
        logger.info("Hello World!")
    }

    override fun onDisable() {
        logger.warning("Bye.")
    }
}