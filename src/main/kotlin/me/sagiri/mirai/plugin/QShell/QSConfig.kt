package me.sagiri.mirai.plugin.QShell

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object QSConfig: AutoSavePluginConfig("QSconfig") {
    @ValueDescription("主人qq号")
    var master by value<Long>(2476255563)

    @ValueDescription("shell列表")
    val shellList : MutableList<CommandConfig> by value(
        mutableListOf(
            CommandConfig(
                "shell",
                "^\\\$(.+)",
                mutableListOf<Long>(0),
                mutableListOf<Long>(0),
                mutableListOf(
                    "bash",
                    "-c",
                    "$1"
                ),
                mutableListOf(
//                    0L
                ),
                true,
                "执行Shell命令",
                "没有执行Shell的权限"
            )
        )
    )
}