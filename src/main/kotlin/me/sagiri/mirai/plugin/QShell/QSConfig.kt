package me.sagiri.mirai.plugin.QShell

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

/**
 * 配置文件
 */
object QSConfig: AutoSavePluginConfig("QSconfig") {
    @ValueDescription("主人qq号")
    var master by value<Long>(2476255563)

    @ValueDescription("shell列表")
    val shellList : MutableList<ShellConfig> by value(
        mutableListOf(
            ShellConfig(
                "shell",
                "^\\\$(.+)",
                mutableListOf<String>(
                ),
                mutableListOf(
                    "bash",
                    "-c",
                    "$1"
                ),
                mutableListOf(

                ),
                mutableListOf(

                ),
                true,
                "执行Shell命令",
                "没有执行Shell的权限",
                "\$msg",
                0L
            )
        )
    )
}