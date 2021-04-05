package me.sagiri.mirai.plugin.QShell

import me.sagiri.mirai.plugin.QShell.QSCommand.enable
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.plugin.jvm.reloadPluginConfig
import net.mamoe.mirai.console.util.ConsoleExperimentalApi


@OptIn(ConsoleExperimentalApi::class)
object QSCommand : CompositeCommand(
    Main, "qs",
    description = "qs"
) {
    lateinit var help: String

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
    @Description("获取shell列表")
    suspend fun CommandSender.list() {
        var msg = ""
        var command = ""
        for (index in QSConfig.shellList.indices) {
            val commandConfig = QSConfig.shellList[index]
            for (index in commandConfig.commandList.indices) {
                command += commandConfig.commandList[index]
                if (index != commandConfig.commandList.size - 1) {
                    command += " "
                }
            }
            var sp = ""
            var spNum = 8 - commandConfig.name.length
            if (spNum > 0) {
                for (i in 1..spNum) {
                    sp += " "
                }
            }
            msg += "${commandConfig.name} $sp${if(commandConfig.isEnabled) "开" else "关"} ${commandConfig.commandRegex}"
            if (index != QSConfig.shellList.size - 1) {
                msg += "\n"
            }
        }
        sendMessage(msg)
    }

    @SubCommand("info")
    @Description("获取Shell的详细配置")
    suspend fun CommandSender.info(shellName: String) {
        var hasShell = false
        QSConfig.shellList.forEach { commandConfig ->
            if (commandConfig.name == shellName) {
                hasShell = true
                var command = ""
                var trust = ""
                var black = ""
                for (index in commandConfig.commandList.indices) {
                    command += commandConfig.commandList[index]
                    if (index != commandConfig.commandList.size - 1) {
                        command += " "
                    }
                }
                for (index in commandConfig.trustList.indices) {
                    trust += "  ${commandConfig.trustList[index]}"
                    if (index != commandConfig.trustList.size - 1) {
                        trust += "\n"
                    }
                }
                for (index in commandConfig.blackList.indices) {
                    black += "  ${commandConfig.blackList[index]}"
                    if (index != commandConfig.blackList.size - 1) {
                        black += "\n"
                    }
                }
                sendMessage("名字: ${commandConfig.name}\n状态: ${if (commandConfig.isEnabled) "开启" else "关闭"}\n说明: ${commandConfig.description}\n正则表达式: ${commandConfig.commandRegex}\n执行命令: $command\n信任: $trust\nblack: $black\n没有权限时的返回消息\n  ${commandConfig.notPresentMessage}")
            }
        }

        if (!hasShell) {
            sendMessage("没有找到 Shell: ${shellName}")
        }
    }

    @SubCommand("echo")
    @Description("回复指定消息")
    suspend fun CommandSender.echo(chat: String) {
        sendMessage(chat)
    }

    @SubCommand("trust")
    @Description("添加用户到信任列表")
    suspend fun CommandSender.add(shellName: String, role: String) {
        var isAdd = false
        QSConfig.shellList.forEach { commandConfig ->
            if (commandConfig.name == shellName) {
                isAdd = true
                if (role !in commandConfig.trustList) {
                    commandConfig.trustList.add(role)
                    sendMessage("已经添加${role}到${shellName}的信任列表")
                    isAdd = true
                } else {
                    sendMessage("${role} 在${shellName}里面了呢")
                    isAdd = true
                }
            }
        }

        if (!isAdd) {
            sendMessage("没有找到 Shell: ${shellName}")
        }
    }

    @SubCommand("deny")
    @Description("从信任列表移除用户")
    suspend fun CommandSender.deny(shellName: String, role: String) {
        var isAdd = false
        QSConfig.shellList.forEach { commandConfig ->
            if (commandConfig.name == shellName) {
                isAdd = true
                if (role in commandConfig.trustList) {
                    commandConfig.trustList.remove(role)
                    sendMessage("已把${role}从${shellName}信任列表移除")
                    isAdd = true
                } else {
                    sendMessage("${role} 不在${shellName}信任列表里")
                    isAdd = true
                }
            }
        }

        if (!isAdd) {
            sendMessage("没有找到 Shell: ${shellName}")
        }
    }



    @SubCommand("denyAll")
    @Description("清楚指定的shell所有信任列表")
    suspend fun CommandSender.clear(shellName: String) {
        var isAdd = false
        QSConfig.shellList.forEach { commandConfig ->
            if (commandConfig.name == shellName) {
                isAdd = true
                commandConfig.trustList.clear()
            }
        }

        if (!isAdd) {
            sendMessage("没有找到 Shell: ${shellName}")
        }
    }

    @SubCommand("black")
    @Description("添加用户到balck列表")
    suspend fun CommandSender.black(shellName: String, role: String) {
        var isAdd = false
        QSConfig.shellList.forEach { commandConfig ->
            if (commandConfig.name == shellName) {
                isAdd = true
                if (role !in commandConfig.blackList) {
                    commandConfig.blackList.add(role)
                    sendMessage("已经添加${role}到${shellName}的black列表")
                    isAdd = true
                } else {
                    sendMessage("${role} 在${shellName}里面了呢")
                    isAdd = true
                }
            }
        }

        if (!isAdd) {
            sendMessage("没有找到 Shell: ${shellName}")
        }
    }

    @SubCommand("blackClear")
    @Description("清除black列表")
    suspend fun CommandSender.blackClear(shellName: String, role: String) {
        var isAdd = false
        QSConfig.shellList.forEach { commandConfig ->
            if (commandConfig.name == shellName) {
                isAdd = true
                commandConfig.blackList.clear()
                sendMessage("black列表已清楚")
            }
        }

        if (!isAdd) {
            sendMessage("没有找到 Shell: ${shellName}")
        }
    }

    @SubCommand("enable")
    @Description("开启一个shell")
    suspend fun CommandSender.enable(shellName: String) {
        var isAdd = false
        QSConfig.shellList.forEach { commandConfig ->
            if (commandConfig.name == shellName) {
                isAdd = true
                if (commandConfig.isEnabled) {
                    sendMessage("${shellName}启用状态  ${commandConfig.description}")
                } else {
                    commandConfig.isEnabled = true
                    sendMessage("已开启${shellName}  ${commandConfig.description}")
                }
            }
        }

        if (!isAdd) {
            sendMessage("没有找到 Shell: ${shellName}")
        }
    }

    @SubCommand("disable")
    @Description("关闭一个shell")
    suspend fun CommandSender.disable(shellName: String) {
        var isAdd = false
        QSConfig.shellList.forEach { commandConfig ->
            if (commandConfig.name == shellName) {
                isAdd = true
                if (!commandConfig.isEnabled) {
                    sendMessage("${shellName}关闭状态  ${commandConfig.description}")
                } else {
                    commandConfig.isEnabled = false
                    sendMessage("已关闭${shellName} ${commandConfig.description}")
                }
            }
        }

        if (!isAdd) {
            sendMessage("没有找到 Shell: ${shellName}")
        }
    }

    @SubCommand("add")
    @Description("添加一个shell")
    suspend fun CommandSender.remove(shellName: String, commandRegex: String, notPresentMessage : String = "") {
        var is_in = false
        QSConfig.shellList.forEach { commandConfig ->
            if (commandConfig.name == shellName) {
                is_in = true
            }
        }

        if (is_in) {
            sendMessage("$shellName 已存在")
        } else {
            QSConfig.shellList.add(
                ShellConfig(
                    shellName,
                    commandRegex,
                    mutableListOf(),
                    mutableListOf(),
                    mutableListOf(),
                    mutableListOf(),
                    true,
                    "description",
                    notPresentMessage,
                    "\$msg",
                    0L,
                )
            )
        }
    }

    @SubCommand("set")
    @Description("设置shell值")
    suspend fun CommandSender.set(shellName: String, type: String, v: String) {
        var isAdd = false
        QSConfig.shellList.forEach { commandConfig ->
            if (commandConfig.name == shellName) {
                isAdd = true
                when (type) {
                    "name", "名字" -> {
                        commandConfig.name = v
                        sendMessage("设置成功")
                    }
                    "description", "描述" -> {
                        commandConfig.description = v
                        sendMessage("设置成功")
                    }
                    "commandRegex", "正则" -> {
                        commandConfig.commandRegex = v
                        sendMessage("设置成功")
                    }
                    "notPresentMessage", "提示" -> {
                        commandConfig.notPresentMessage = v
                        sendMessage("设置成功")
                    }
                    else -> {
                        sendMessage("错误的选项\nname 名字\ndescription 描述\ncommandRegex 正则\nnotPresentMessage 提示")
                    }
                }
            }
        }

        if (!isAdd) {
            sendMessage("没有找到 Shell: ${shellName}")
        }
    }

    @SubCommand("remove")
    @Description("移除shell")
    suspend fun CommandSender.remove(shellName: String) {
        var is_in = false
        QSConfig.shellList.forEach { commandConfig ->
            if (commandConfig.name == shellName) {
                is_in = true
                QSConfig.shellList.remove(commandConfig)
                sendMessage("shell $shellName 已移除")
            }
        }
        if (is_in.not()) {
            sendMessage("$shellName 不存在")
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
                    "show", "查看" -> {
                        println(commandConfig.commandList.toString())
                    }
                    "add", "添加" -> {
                        commandConfig.commandList.add(v)
                    }
                    "remove", "移除" -> {
                        commandConfig.commandList.remove(commandConfig)
                    }
                    "clear", "清空" -> {
                        commandConfig.commandList.clear()
                    }
                    "last", "最后" -> {
                        commandConfig.commandList.removeLast()
                    }
                    else -> {
                        sendMessage("add 添加\nremove移除指定值\nclear清除所有\n删除最后一个")
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