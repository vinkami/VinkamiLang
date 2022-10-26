package com.vinkami.vinkamilang

import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused")
class VinkamiLang: JavaPlugin() {
    override fun onEnable() {
        val pf = PathFinder(this)
        val vk = getCommand("vk")!!
        vk.setExecutor(pf.cmd)
        vk.tabCompleter = pf.cmdtc

        saveDefaultConfig()
        logger.info("Hello World!")
    }

    override fun onDisable() {
        logger.warning("Bye.")
    }
}