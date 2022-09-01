package com.vinkami.vinkamilang

import org.bukkit.plugin.java.JavaPlugin

class VinkamiLang : JavaPlugin() {
    override fun onEnable() {
        val pf = PathFinder(this)
        getCommand("vk")!!.setExecutor(pf.cmd)

        saveDefaultConfig()
        logger.info("Hello World!")
    }

    override fun onDisable() {
        logger.warning("Bye.")
    }
}