package me.sagiri.mirai.plugin.QShell

import kotlinx.coroutines.*
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.console.permission.PermissionService.Companion.permit
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.disable
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.Stranger
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.info
import okhttp3.MediaType.Companion.toMediaType
import java.util.regex.Pattern

object Main : KotlinPlugin(
    JvmPluginDescription(
        id = "me.sagiri.mirai.plugin.QShell",
        name = "QShell",
        version = "0.1.0"
    ) {
        author("sagiri")

        info("""
            QShell
        """.trimIndent())

    }
) {
    lateinit var shell : Shell
    lateinit var messageEventListener : CompletableJob

    @OptIn(ConsoleExperimentalApi::class, ExperimentalCommandDescriptors::class)
    override fun onEnable() {
        logger.info { "Plugin loaded" }
        //加载配置
        QSConfig.reload()
        shell = Shell()
        // 注册命令
        CommandManager.registerCommand(QSCommand)

        AbstractPermitteeId.parseFromString("u${QSConfig.master}").permit(QSCommand.permission)

        // 注册事件
        messageEventListener = globalEventChannel().subscribeAlways<MessageEvent> { event ->
            QSConfig.shellList.forEach list@{ commandConfig ->
                    val pattern = Pattern.compile(commandConfig.commandRegex, Pattern.DOTALL).matcher(message.content)
                    if (pattern.find()) {
                        if(commandConfig.isEnabled) {
                            if(event.subject is Group) {
                                if(0L !in commandConfig.group && event.subject.id !in commandConfig.group && event.sender.id != QSConfig.master) {
                                    logger.info("群: ${(event.subject as Group).name} ${event.subject.id} 成员 ${event.sender.nick} ${event.sender.id} 尝试执行 ${event.message.content} 不适用")
                                    return@list
                                }
                            }

                            if(event.subject is Friend) {
                                if(0L !in commandConfig.friend && event.subject.id !in commandConfig.friend && event.sender.id != QSConfig.master) {
                                    logger.info("好友 ${(event.subject as Friend).nick} 尝试执行 ${event.message.content} 不适用")
                                    return@list
                                }
                            }

                            if(event.subject is Member || event.subject is Stranger) {
                                event.subject.sendMessage("加我为好友")
                                return@list
                            }

                        if (0L in commandConfig.trustList || event.sender.id == QSConfig.master || event.sender.id in commandConfig.trustList) {
                            val groupCount = pattern.groupCount()
                            val tempCommandConfig = commandConfig.commandList.toMutableList()
                            for(index in tempCommandConfig.indices) {
                                // 替换变量 $<数字> 为对应的分组
                                val varPattern = Pattern.compile("\\\$(\\d)", Pattern.DOTALL).matcher(tempCommandConfig[index])
                                if(varPattern.find()) {
                                    val groupIndex = varPattern.group(1).toInt()
                                    if(groupIndex <= groupCount || groupIndex > 0) {
                                        tempCommandConfig[index] = tempCommandConfig[index].replace("\$${groupIndex}", pattern.group(groupIndex))
                                    }
                                }
                            }
                            GlobalScope.launch {
                                val result = withTimeout(600000L) {
                                    shell.exec(tempCommandConfig)?.let { event.subject.sendMessage(it) }
                                }
                            }
                        } else {
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
        messageEventListener.complete()
    }
}