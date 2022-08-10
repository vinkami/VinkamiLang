package com.vinkami.vinkamilang.script

import com.vinkami.vinkamilang.PathFinder
import java.io.File

class Script(file: File, pf: PathFinder) {
    var code = ""
    val name = file.relativeTo(pf.plugin.dataFolder.resolve("scripts"))
                   .toString()
                   .replace("\\", ".")
                   .replace(".vk", "")
    init {
        file.forEachLine {
            code += it + "\n"
        }
    }

    override fun toString(): String {
        return "<Script $name>"
    }
}