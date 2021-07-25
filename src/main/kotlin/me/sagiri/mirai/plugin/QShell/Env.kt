package me.sagiri.mirai.plugin.QShell

import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.content

/**
 * 变量环境
 */
object Env {
    /**
     * 通过消息事件返回变量环境
     */
    suspend fun getMap(messageEvent: MessageEvent): Map<String, String> {
        val sender = messageEvent.sender
        val senderProfile = sender.queryProfile()
        val message = messageEvent.message
        val bot = messageEvent.bot
        val properties = System.getProperties()

        val envMap = hashMapOf<String,String>(
            // QSconfig 相关的变量
            "MASTER" to QSConfig.master.toString(),
            "SHELLENGTH" to QSConfig.shellList.size.toString(),

            // MessageEvent 相关的变量
            // 消息发送者相关的变量
            "SENDERNAME" to sender.nick,
            "SENDERID" to sender.id.toString(),

            "SENDERSIGN" to senderProfile.sign,
            "SENDEREMAIL" to senderProfile.email,
            "SENDERNAME" to senderProfile.nickname,
            "SENDERLEVEL" to senderProfile.qLevel.toString(),
            "SENDERAGE" to senderProfile.age.toString(),
            "SENDERSEX" to senderProfile.sex.toString(),

            // 消息相关的变量
            "MESSAGECONTENT" to message.content,
            "MESSAGEMIRAI" to message.serializeToMiraiCode(),

            // 机器人相关的变量
            "BOTNAME" to bot.nick,
            "BOTAVATARURL" to bot.avatarUrl,
            "BOTFRIENDSSIZE" to bot.friends.size.toString(),
            "BOTGROUPSSIZE" to bot.groups.size.toString(),

            // JAVA相关变量
            "JAVAVMNAME" to properties.get("java.vm.name") as String,
            "JAVAVERSION" to properties.get("java.version") as String,

            //操作系统相关变量
            "OSARCH" to properties.get("os.arch") as String,
            "OSNAME" to properties.get("os.name") as String
        )

        return envMap
    }
}