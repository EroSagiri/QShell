package me.sagiri.mirai.plugin.QShell

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object QSConfig: AutoSavePluginConfig("QSconfig") {
    @ValueDescription("主人qq号")
    var master by value<Long>(2476255563)

//    @ValueDescription("匹配命令的正则表达式")
//    var cmdRegex by value<String>("^\\\$(.+)")
//
//    @ValueDescription("可以执行命令的qq列表")
//    var trusts: MutableList<Long> by value()

//    @ValueDescription("执行命令的解析器")
//    var shell by value<String>("bash")
//
//    @ValueDescription("执行命令解析器的选项")
//    var shell_options by value<String>("-c")
//
//    @ValueDescription("执行命令格式化（\$cmd 替换成执行的命令)")
//    val shell_command by value<String>("\$cmd")

    @ValueDescription("命令列表(\$cmd 回被替换成要执行的命令)")
    val commandList : MutableList<String> by value(mutableListOf("bach", "-c", "\$cmd"))

    val shellList : MutableList<CommandConfig> by value(
        mutableListOf(
            CommandConfig(
                "shell",
                "^\\\$(.+)",
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