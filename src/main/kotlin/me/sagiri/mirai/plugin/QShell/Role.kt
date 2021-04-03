package me.sagiri.mirai.plugin.QShell

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.data.UserProfile
import net.mamoe.mirai.event.events.MessageEvent
import kotlin.random.Random

object Role {
    /**
     * 检查这个消息发送者是否有权限执行这个shell
     */
    suspend fun hasPermission(event: MessageEvent, shellConfig: ShellConfig): Boolean {
        // 是主人的话可以哦
        if (event.sender.id == QSConfig.master) return true
        // 黑名单
        shellConfig.blackList.forEach { role ->
            if (roleRegex(role, event)) return false
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

    /**
     * 规则是一个字符串
     * 第一个字符
     * f 是好友 g群组 u一个qq用户 *任何人 t临时会话 x性别 人随机的
     */
    private suspend fun roleRegex(role: String, event: MessageEvent): Boolean {
        // 长度为0,直接返回false
        if (role.isEmpty()) return false
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
                            if (tmp.length == 1)
                                if (orp) return@and else return@or
                            when (tmp[1]) {
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
                            if (Random.nextBoolean())
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
            if (!orp) {
                return false
            }
        }

        // 如果所有与规则都没有被退出,这个语句才会被执行
        return true
    }
}