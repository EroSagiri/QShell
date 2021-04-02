package me.sagiri.mirai.plugin.QShell

import kotlinx.coroutines.*
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.console.permission.PermissionService.Companion.permit
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.data.UserProfile
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.utils.info
import java.util.*
import java.util.regex.Pattern
import kotlin.random.Random

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
                        if (hasPermission(event, commandConfig)) {
                            val groupCount = pattern.groupCount()
                            val tempCommandConfig = commandConfig.commandList.toMutableList()
                            for (index in tempCommandConfig.indices) {
                                // 替换变量 $<数字> 为对应的分组
                                val varPattern =
                                    Pattern.compile("\\\$(\\d)", Pattern.DOTALL).matcher(tempCommandConfig[index])
                                if (varPattern.find()) {
                                    val groupIndex = varPattern.group(1).toInt()
                                    if (groupIndex <= groupCount || groupIndex > 0) {
                                        var groupContent = pattern.group(groupIndex)
                                        commandConfig.replace.forEach { it ->
                                            val t = it.split(";;")
                                            if(t.size > 1) {
                                                groupContent = groupContent.replace(t[0], t[1])
                                            }
                                        }
                                        tempCommandConfig[index] =
                                            tempCommandConfig[index].replace("\$${groupIndex}", groupContent)
                                    }
                                }
                                // 变量替换
                                tempCommandConfig[index] =
                                    tempCommandConfig[index].replace("\$senderId", event.sender.id.toString())
                                tempCommandConfig[index] =
                                    tempCommandConfig[index].replace("\$senderName", event.sender.nick)
                                tempCommandConfig[index] =
                                    tempCommandConfig[index].replace("\$time", event.time.toString())
                            }

                            val shell = Shell()
                            /**
                             * 设置变量环境
                             */
                            shell.environment["MASTER"] = QSConfig.master.toString()

                            shell.environment["BOTID"] = bot.id.toString()
                            shell.environment["BOTNAME"] = bot.nick
                            shell.environment["BOTFRIENDSSIZE"] = bot.friends.size.toString()
                            shell.environment["BOTGROUPSSIZE"] = bot.groups.size.toString()
                            shell.environment["BOTAVATARURL"] = bot.avatarUrl

                            shell.environment["MESSAGECONTENT"] = message.content
                            shell.environment["MESSAGEMIRAI"] = message.serializeToMiraiCode()
                            shell.environment["MESSAGELENGTH"] = message.content.length.toString()

                            shell.environment["SUBJECTNAME"] = subject.javaClass.name
                            shell.environment["SENDERID"] = sender.id.toString()
                            shell.environment["SENDERNAME"] = sender.nick
                            shell.environment["SENDERAGE"] = sender.queryProfile().age.toString()
                            shell.environment["SENDEREMAIL"] = sender.queryProfile().email
                            shell.environment["SENDERLEVEL"] = sender.queryProfile().qLevel.toString()
                            shell.environment["SENDERSEX"] = sender.queryProfile().sex.name
                            shell.environment["SENDERSIGN"] = sender.queryProfile().sign



                            val l = GlobalScope.launch {
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
        messageEventListener.complete()
    }

    /**
     * 检查这个消息发送者是否有权限执行这个shell
     */
    private suspend fun hasPermission(event : MessageEvent, shellConfig : ShellConfig): Boolean {
        // 是主人的话可以哦
        if(event.sender.id == QSConfig.master) return true
        // 黑名单
        shellConfig.blackList.forEach { role ->
            if(roleRegex(role, event)) return false
        }
        // 遍历匹配规则
        shellConfig.trustList.forEach { role ->
            // 如果这个规则符合,返回true
            if (roleRegex(role, event)) {
                return true
            }
        }
        // 如果所有规则都不符合,返回false
        return false
    }

    private suspend fun roleRegex(role : String, event : MessageEvent) : Boolean {
        // 长度为0,直接返回false
        if(role.isEmpty()) return false
        var ands = role.split("&")
        ands.forEach { and ->
            val ors = and.split("|")
            var orp = false
            run and@{
                ors.forEach or@{ it ->
                    var tmp = it
                    // 反转了
                    while (tmp[0] == '!') {
                        orp = !orp
                        tmp = tmp.removeRange(IntRange(0, 0))
                    }
                    when (tmp[0]) {
                        // 任何
                        '*' -> {
                            orp = !orp
                            if (orp) {
                                return@and

                            } else {
                                return@or
                            }
                        }
                        // 好友
                        'f' -> {
                            // 判断是否是好友,如果不是跳出或
                            event.bot.getFriend(event.sender.id) ?: return@or
                            // 判断是否是通配符*,如果是直接跳出与
                            if (tmp[1] == '*') {
                                orp = !orp
                                if (orp) {
                                    return@and
                                } else {
                                    return@or
                                }
                            }
                            try {
                                val qq = tmp.removeRange(IntRange(0, 0)).toLong()
                                if (event.sender.id == qq) {
                                    orp = !orp
                                    if (orp) {
                                        return@and
                                    } else {
                                        return@or
                                    }
                                }
                            } catch (e: NumberFormatException) {
                                // 转换长整形失败,跳出这个or
                                if (orp) {
                                    return@and
                                } else {
                                    return@or
                                }
                            }
                        }
                        // 群
                        'g' -> {
                            if (event.sender !is Group) return@or
                            try {
                                val groupId = tmp.removeRange(IntRange(0, 0)).toLong()
                                if (event.sender.id == groupId) {
                                    orp = !orp
                                    if (orp) {
                                        return@and
                                    } else {
                                        return@or
                                    }
                                }
                            } catch (e: NumberFormatException) {
                                // 转换长整形失败,跳出这个or
                                if (orp) {
                                    return@and
                                } else {
                                    return@or
                                }
                            }
                        }
                        // 临时回话
                        't' -> {
                            if (event.subject !is Member) return@or
                            if (tmp[1] == '*') {
                                orp = !orp
                                if (orp) {
                                    return@and
                                } else {
                                    return@or
                                }
                            }
                            try {
                                val qq = tmp.removeRange(IntRange(0, 0)).toLong()
                                if (event.sender.id == qq) {
                                    orp = !orp
                                    if (orp) {
                                        return@and
                                    } else {
                                        return@or
                                    }
                                }
                            } catch (e: NumberFormatException) {
                                if (orp) {
                                    return@and
                                } else {
                                    return@or
                                }
                            }
                        }
                        // 用户
                        'u' -> {
                            if (tmp[1] == '*') {
                                orp = !orp
                                if (orp) {
                                    return@and
                                } else {
                                    return@or
                                }
                            }
                            try {
                                val qq = tmp.removeRange(IntRange(0, 0)).toLong()
                                if (event.sender.id == qq) {
                                    orp = !orp
                                }
                                if (orp) {
                                    return@and
                                } else {
                                    return@or
                                }
                            } catch (e: NumberFormatException) {
                                if (orp) {
                                    return@and
                                } else {
                                    return@or
                                }
                            }
                        }
                        //
                        // 陌生人
                        's' -> {

                        }
                        // 性别
                        'x' -> {
                            if(tmp.length == 1)
                                if (orp) return@and else return@or
                            when(tmp[1]) {
                                // 男孩子
                                '♀' -> {
                                    val profile = event.sender.queryProfile()
                                    if (profile.sex == UserProfile.Sex.MALE) {
                                        orp = !orp
                                    }
                                    if (orp) {
                                        return@and
                                    } else {
                                        return@or
                                    }
                                }
                                // 女孩子
                                '♂' -> {
                                    val profile = event.sender.queryProfile()
                                    if (profile.sex == UserProfile.Sex.FEMALE) {
                                        orp = !orp
                                    }
                                    if (orp) {
                                        return@and
                                    } else {
                                        return@or
                                    }
                                }
                            }
                        }
                        'r' -> {
                            if(Random.nextBoolean())
                                orp = !orp
                            if (orp) return@and else return@or
                        }
                        // 无法理解
                        else -> {
                            if (orp) {
                                return@and
                            } else {
                                return@or
                            }
                        }
                    }
                }
            }
            // orp这个值如果没有改变,说明已经至少有一项与匹配不符合,没必要去继续判断了,直接退出这条规则,这条规则的返回值是false,如果这个值改变继续与规则
            if(!orp) {
                return false
            }
        }

        // 如果所有与规则都没有被退出,这个语句才会被执行
        return true
    }
}