package com.vinkami.vinkamilang

import org.bukkit.plugin.java.JavaPlugin

class VinkamiLang: JavaPlugin() {
    override fun onEnable() {
        val pf = PathFinder(this)
        val vk = getCommand("vk")!!
        vk.setExecutor(pf.cmd)
        vk.tabCompleter = pf.cmd

        saveDefaultConfig()
        logger.info("Hello World!")
    }

    override fun onDisable() {
        logger.warning("Bye.")
    }
}