package com.vinkami.vinkamilang.script

import com.vinkami.vinkamilang.PathFinder
import java.io.File

class Script(private val file: File, private var pf: PathFinder) {
    private var code = ""
    private val name = this.file.relativeTo(this.pf.plugin.dataFolder.resolve("scripts"))
                           .toString()
                           .replace("\\", ".")
                           .replace(".vk", "")
    init {
        file.forEachLine {
            code += it + "\n"
        }
    }

    override fun toString(): String {
        return this.name
    }
}