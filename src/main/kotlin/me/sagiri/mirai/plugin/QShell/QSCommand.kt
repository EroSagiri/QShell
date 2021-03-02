package me.sagiri.mirai.plugin.QShell

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.util.ConsoleExperimentalApi


@OptIn(ConsoleExperimentalApi::class)
object QSCommand : CompositeCommand(
    Main, "qs", "QS",
    description = "qs"
) {
    lateinit var help : String

    init {
        val helpFilePath = "help.txt"
        val helpFile = javaClass.classLoader.getResource(helpFilePath)
        help = helpFile.readText()

    }
    @SubCommand("help")
    @Description("获取帮助")
    suspend fun CommandSender.help() {
        sendMessage(help)
    }

    @SubCommand("add")
    @Description("添加到信任列表")
    suspend fun ConsoleCommandSender.add(id : Long) {
        if(id !in QSConfig.trusts) {
            QSConfig.trusts.add(id)
            sendMessage("$id 添加成功")
        } else {
            sendMessage("$id 已经在信任列表")
        }
    }

    @SubCommand("remove")
    @Description("从信任列表信任列表移除")
    suspend fun ConsoleCommandSender.remove(id : Long) {
        if(id in QSConfig.trusts) {
            QSConfig.trusts.remove(id)
        } else {
            sendMessage("$id 不在信任列表")
        }
    }

    @SubCommand("list")
    @Description("列出信任列表")
    suspend fun ConsoleCommandSender.list() {
        QSConfig.trusts.forEach{
            sendMessage(it.toString())
        }
    }

    @SubCommand("clear")
    @Description("清楚信任列表")
    suspend fun ConsoleCommandSender.clear() {
        QSConfig.trusts.clear()
        sendMessage("清楚成功")
    }
}