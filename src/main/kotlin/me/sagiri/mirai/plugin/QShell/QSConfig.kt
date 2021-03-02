package me.sagiri.mirai.plugin.QShell

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object QSConfig: AutoSavePluginConfig("QSconfig") {
    @ValueDescription("主人qq号")
    var master by value<Long>(2476255563)
    @ValueDescription("匹配命令的正则表达式")
    var cmdRegex by value<String>("^\\\$(.+)")
    @ValueDescription("可以执行命令的qq列表")
    var trusts: MutableList<Long> by value()
}