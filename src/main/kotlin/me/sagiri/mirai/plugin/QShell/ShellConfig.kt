package me.sagiri.mirai.plugin.QShell

import kotlinx.serialization.Serializable

/**
 * 命令解析器配置
 * @param name 名字
 * @param commandList 命令行，第一个是可执行程序，后面的是参数
 * @param commandRegex 匹配qq消息的正则表达式 第X个分组会替换命令行的$X,X是正整数
 * @param replace 执行前替换执行的命令的 是一个字符串 左边是替换的字符串用;隔开右是替换的字符串
 * @param trustList 能执行命令的用户 * 代表所有人， u[qq]代表qq用户， f[qq]代表qq好友， g[q群号码]qq群
 * @param blackList 黑名单
 * @param isEnabled 是否启用
 * @param description 说明
 * @param notPresentMessage 没有权限的提示
 * @param message  里面的 $msg 会被替换从标准（错误）输出
 * @param timeout 超时时间
 */
@Serializable
data class ShellConfig(
    var name : String,
    var commandRegex : String,
    var replace : MutableList<String>,
    val commandList: MutableList<String>,
    val trustList : MutableList<String>,
    val blackList : MutableList<String>,
    var isEnabled : Boolean,
    var description : String,
    var notPresentMessage : String,
    var message : String,
    var timeout : Long
    )