package me.sagiri.mirai.plugin.QShell

import kotlinx.serialization.Serializable

/**
 * 命令解析器配置
 * @name 命令解析器名字
 * @commandRegex 匹配命令的正则表达式
 * @commandList 命令执行列表
 */
@Serializable
class CommandConfig(var name : String, var commandRegex : String, val commandList: MutableList<String>, val trustList : MutableList<Long>, var isEnabled : Boolean, var description : String, var notPresentMessage : String)