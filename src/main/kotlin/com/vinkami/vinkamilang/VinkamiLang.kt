package com.vinkami.vinkamilang

import org.bukkit.plugin.java.JavaPlugin

class VinkamiLang : JavaPlugin() {
    override fun onEnable() {
        PathFinder(this)

        this.saveDefaultConfig()
        logger.info("Hello World!")
    }

    override fun onDisable() {
        logger.warning("Bye.")
    }
}