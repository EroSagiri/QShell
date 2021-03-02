package me.sagiri.mirai.plugin.QShell

import kotlinx.coroutines.CoroutineExceptionHandler
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.info
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
    private var qsconfig  = QSConfig

    @OptIn(ConsoleExperimentalApi::class, ExperimentalCommandDescriptors::class)
    override fun onEnable() {
        logger.info { "Plugin loaded" }
        shell = Shell()
        //加载配置
        QSConfig.reload()
        // 注册命令
        CommandManager.registerCommand(QSCommand)

        // 注册事件
        globalEventChannel().subscribeAlways(
            MessageEvent::class,
            CoroutineExceptionHandler { _, throwable ->
                logger.info(throwable)
            },
            priority = EventPriority.MONITOR
            ) call@{
        }

        globalEventChannel().subscribeAlways(
            FriendMessageEvent::class
        ) {
            val p = Pattern.compile(qsconfig.cmdRegex).matcher(message.content)
            if(p.find()) {
                if(isPer(sender.id)) {
                    val cmd = p.group(1)
                    shell.exec(cmd)?.let { it1 -> subject.sendMessage(it1) }
                } else {
                    subject.sendMessage("没有权限")
                }
            }
        }

        globalEventChannel().subscribeAlways(
            GroupMessageEvent::class
        ) {

            val p = Pattern.compile(qsconfig.cmdRegex).matcher(message.content)
            if(p.find()) {
                if(isPer(sender.id)) {
                    val cmd = p.group(1)
                    shell.exec(cmd)?.let { it1 -> subject.sendMessage(it1) }
                } else {
                    subject.sendMessage("没有权限")
                }
            }
        }
    }
    override fun onDisable() {

    }

    private fun isPer(qq : Long) : Boolean {
        return if(qsconfig.trusts.size >= 1 && qsconfig.trusts[0].toInt() == 0) {
            true
        } else {
            qsconfig.master == qq || qq in qsconfig.trusts
        }
    }
}