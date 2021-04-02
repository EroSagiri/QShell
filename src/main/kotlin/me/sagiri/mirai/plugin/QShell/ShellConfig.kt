package me.sagiri.mirai.plugin.QShell

import kotlinx.serialization.Serializable

/**
 * 命令解析器配置
 * @name 命令解析器名字
 * @commandRegex 匹配命令的正则表达式
 * @commandList 命令执行列表
 * @trustList 信任列表
 * @isEnabled 启用状态
 * @notPresentMessage 没有权限时的提示
 */
@Serializable
class ShellConfig(
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

@Serializable
class Replace(var old : String, var new : String)