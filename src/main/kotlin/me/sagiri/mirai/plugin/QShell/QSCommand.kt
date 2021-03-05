package me.sagiri.mirai.plugin.QShell

import io.ktor.client.request.forms.*
import me.sagiri.mirai.plugin.QShell.QSCommand.add
import me.sagiri.mirai.plugin.QShell.QSCommand.remove
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.plugin.jvm.reloadPluginConfig
import net.mamoe.mirai.console.util.ConsoleExperimentalApi


@OptIn(ConsoleExperimentalApi::class)
object QSCommand : CompositeCommand(
    Main, "qs",
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

    @SubCommand("list")
    @Description("获取Shell列表")
    suspend fun CommandSender.list() {
        var msg = ""
        for(index in QSConfig.shellList.indices) {
            val commandConfig = QSConfig.shellList[index]
            msg += "${commandConfig.name} ${commandConfig.description} ${commandConfig.commandRegex} ${ if(commandConfig.isEnabled) "开启" else "关闭"} 信任 ${commandConfig.trustList.toString()}"
            if(index != QSConfig.shellList.size-1) {
                msg += "\n"
            }
        }
        sendMessage(msg)
    }

    @SubCommand("echo")
    @Description("回复指定消息")
    suspend fun CommandSender.echo(chat : String) {
        sendMessage(chat)
    }

    @SubCommand("trust")
    @Description("添加用户到信任列表")
    suspend fun CommandSender.add(shellName : String, qq : Long) {
        var isAdd = false
        QSConfig.shellList.forEach { commandConfig ->
            if(commandConfig.name == shellName) {
                if(qq !in commandConfig.trustList) {
                    commandConfig.trustList.add(qq)
                    sendMessage("已经添加${qq}到${shellName}的信任列表")
                    isAdd = true
                } else {
                    sendMessage("${qq} 在${shellName}里面了呢")
                    isAdd = true
                }
            }
        }

        if(!isAdd) {
            sendMessage("没有找到 Shell: ${shellName}")
        }
    }

    @SubCommand("deny")
    @Description("从信任列表移除用户")
    suspend fun CommandSender.deny(shellName: String, qq : Long) {
        var isAdd = false
        QSConfig.shellList.forEach { commandConfig ->
            if(commandConfig.name == shellName) {
                if(qq in commandConfig.trustList) {
                    commandConfig.trustList.remove(qq)
                    sendMessage("已把${qq}从${shellName}信任列表移除")
                    isAdd = true
                } else {
                    sendMessage("${qq} 不在${shellName}信任列表里")
                    isAdd = true
                }
            }
        }

        if(!isAdd) {
            sendMessage("没有找到 Shell: ${shellName}")
        }
    }

    @SubCommand("denyAll")
    @Description("清楚所有")
    suspend fun CommandSender.clear(shellName: String) {
        var isAdd = false
        QSConfig.shellList.forEach { commandConfig ->
            if(commandConfig.name == shellName) {

            }
        }

        if(!isAdd) {
            sendMessage("没有找到 Shell: ${shellName}")
        }
    }

    @SubCommand("enable")
    @Description("开启一个shell")
    suspend fun CommandSender.enable(shellName: String) {
        var isAdd = false
        QSConfig.shellList.forEach { commandConfig ->
            if(commandConfig.name == shellName) {
                if(commandConfig.isEnabled) {
                    sendMessage("${shellName}启用状态  ${commandConfig.description}")
                } else {
                    commandConfig.isEnabled = true
                    sendMessage("已开启${shellName}  ${commandConfig.description}")
                }
            }
        }

        if(!isAdd) {
            sendMessage("没有找到 Shell: ${shellName}")
        }
    }

    @SubCommand("disable")
    @Description("关闭一个shell")
    suspend fun CommandSender.disable(shellName: String) {
        var isAdd = false
        QSConfig.shellList.forEach { commandConfig ->
            if(commandConfig.name == shellName) {
                if(!commandConfig.isEnabled) {
                    sendMessage("${shellName}关闭状态  ${commandConfig.description}")
                } else {
                    sendMessage("已关闭${shellName} ${commandConfig.description}")
                }
            }
        }

        if(!isAdd) {
            sendMessage("没有找到 Shell: ${shellName}")
        }
    }

    @SubCommand("add")
    @Description("添加一个shell")
    suspend fun CommandSender.remove(shellName: String, commandRegex : String) {
        var is_in = false
        QSConfig.shellList.forEach { commandConfig ->
            if(commandConfig.name == shellName) {
                is_in = true
            }
        }

        if(is_in) {
            sendMessage("${shellName} 已存在")
        } else {
            QSConfig.shellList.add(
                CommandConfig(
                shellName,
                commandRegex,
                mutableListOf(),
                    mutableListOf(),
                    true,
                    "description",
                    "没有执行${shellName}的权限"
            ))
        }
    }

    @SubCommand("set")
    @Description("设置shell值")
    suspend fun CommandSender.set(shellName: String, type: String, v : String) {
        var isAdd = false
        QSConfig.shellList.forEach { commandConfig ->
            if(commandConfig.name == shellName) {
                when(type) {
                    "name" -> {
                        commandConfig.name = v
                    }
                    "description" -> {
                        commandConfig.description = v
                    }
                    "commandRegex" -> {
                        commandConfig.commandRegex = v
                    }
                    "notPresentMessage" -> {
                        commandConfig.notPresentMessage = v
                    }
                }
            }
        }

        if(!isAdd) {
            sendMessage("没有找到 Shell: ${shellName}")
        }
    }

    @SubCommand("cmd")
    @Description("编辑cmd")
    suspend fun CommandSender.cmd(shellName: String, type: String, v: String) {
        var isAdd = false
        QSConfig.shellList.forEach { commandConfig ->
            if(commandConfig.name == shellName) {
                isAdd = true
                when(type) {
                    "show" -> {
                        println(commandConfig.commandList.toString())
                    }
                    "add" -> {
                        commandConfig.commandList.add(v)
                    }
                    "remove" -> {
                        commandConfig.commandList.remove(commandConfig)
                    }
                    "clear" -> {
                        commandConfig.commandList.clear()
                    }
                    "last" -> {
                        commandConfig.commandList.removeLast()
                    }
                    else -> {
                        println("add 添加\nremove移除指定值\nclear清除所有\n删除最后一个")
                    }
                }
            }
        }

        if(!isAdd) {
            sendMessage("没有找到 Shell: ${shellName}")
        }
    }

    @SubCommand("reload")
    @Description("重新加载")
    suspend fun CommandSender.disable() {
        Main.reloadPluginConfig(QSConfig)
    }
}