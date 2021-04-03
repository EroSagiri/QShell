package me.sagiri.mirai.plugin.QShell

import kotlinx.coroutines.*
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.console.permission.PermissionService.Companion.cancel
import net.mamoe.mirai.console.permission.PermissionService.Companion.permit
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.utils.info
import java.util.*
import java.util.regex.Pattern

object Main : KotlinPlugin(
    JvmPluginDescription(
        id = "me.sagiri.mirai.plugin.QShell",
        name = "QShell",
        version = "0.1.0"
    ) {
        author("sagiri")

        info(
            """
            QShell
        """.trimIndent()
        )

    }
) {
    lateinit var messageEventListener: CompletableJob

    @OptIn(ConsoleExperimentalApi::class, ExperimentalCommandDescriptors::class)
    override fun onEnable() {
        logger.info { "Plugin loaded" }
        //加载配置
        QSConfig.reload()
        // 注册命令
        CommandManager.registerCommand(QSCommand)

        AbstractPermitteeId.parseFromString("u${QSConfig.master}").permit(QSCommand.permission)

        // 注册事件
        messageEventListener = globalEventChannel().subscribeAlways<MessageEvent> { event ->
            QSConfig.shellList.forEach list@{ commandConfig ->
                // 匹配正则表达式
                val pattern = Pattern.compile(commandConfig.commandRegex, Pattern.DOTALL).matcher(message.content)
                if (pattern.find()) {
                    // 这个shell是否是启用状态
                    if (commandConfig.isEnabled) {
                        // 检查发送者权限
                        if (Role.hasPermission(event, commandConfig)) {
                            val groupCount = pattern.groupCount()
                            val tempCommandConfig = commandConfig.commandList.toMutableList()
                            // docker 变量在指令数组的位置
                            var dockerenvIndex = -1

                            // 对命令数组的处理
                            for (index in tempCommandConfig.indices) {
                                // 替换变量 $<数字> 为对应的分组
                                val varPattern =
                                    Pattern.compile("\\\$(\\d)", Pattern.DOTALL).matcher(tempCommandConfig[index])
                                if (varPattern.find()) {
                                    val groupIndex = varPattern.group(1).toInt()
                                    if (groupIndex <= groupCount || groupIndex > 0) {
                                        var groupContent = pattern.group(groupIndex)
                                        // 替换输入指令
                                        commandConfig.replace.forEach { it ->
                                            val t = it.split(";;")
                                            if(t.size > 1) {
                                                groupContent = groupContent.replace(t[0], t[1])
                                            }
                                        }
                                        // 替换 $x 成对应的分组
                                        tempCommandConfig[index] =
                                            tempCommandConfig[index].replace("\$${groupIndex}", groupContent)
                                    }
                                }

                                if(Pattern.compile("DOCKERENV").matcher(tempCommandConfig[index]).find())
                                    dockerenvIndex = index;
                            }

                            val shell = Shell()
                            /**
                             * 设置变量环境
                             */
                            val envMap = Env.getMap(this)

                            shell.environment.plusAssign(envMap)

                            // 在docker的变量环境
                            if(dockerenvIndex >= 0) {
                                tempCommandConfig.removeAt(dockerenvIndex)
                                val dockerEnvArray = mutableListOf<String>()
                                envMap.forEach { name, value ->
                                    dockerEnvArray.add("-e")
                                    dockerEnvArray.add("$name=$value")
                                }
                                tempCommandConfig.addAll(dockerenvIndex, dockerEnvArray)
                            }

                            // 在这个协程执行shell
                            val l = GlobalScope.launch {
                                logger.info("${sender.id} ${sender.nick} 执行 ${commandConfig.name}")
                                val msg = shell.exec(tempCommandConfig)
                                if (msg != null) {
                                    val t = commandConfig.message.replace("\$msg", msg).deserializeMiraiCode()
                                    event.subject.sendMessage(t)
                                }
                            }
                            if (commandConfig.timeout != 0L) {
                                GlobalScope.launch {
                                    delay(commandConfig.timeout)
                                    if (l.isActive) {
                                        shell.destroy()
                                        event.subject.sendMessage(PlainText("超时被摧毁 Pid: ${shell.getPid()}").plus(event.message.quote()))
                                    }
                                }
                            }
                        } else {
                            if(commandConfig.notPresentMessage.isNotEmpty())
                            event.subject.sendMessage(commandConfig.notPresentMessage)
                        }
                    }
                }
            }
        }
    }

    /**
     * 关闭插件
     */
    override fun onDisable() {
        AbstractPermitteeId.parseFromString("u${QSConfig.master}").cancel(QSCommand.permission, true)
        messageEventListener.complete()
    }
}