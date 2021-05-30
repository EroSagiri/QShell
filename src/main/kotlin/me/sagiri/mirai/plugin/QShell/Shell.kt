package me.sagiri.mirai.plugin.QShell

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import kotlin.properties.Delegates

class Shell {
    private lateinit var pb : ProcessBuilder
    private lateinit var ps : Process
    private var pid by Delegates.notNull<Long>()
    lateinit var environment: MutableMap<String, String>

    init {
        pb = ProcessBuilder()
        environment = pb.environment()
    }

//    fun exec(cmd : String): ShellReturn {
//        pb.command(cmd)
//
//        val ps = pb.start()
//
//        val stdout = BufferedReader(InputStreamReader(ps.inputStream)).readText()
//        val stderr = BufferedReader(InputStreamReader(ps.errorStream)).readText()
//
//        return ShellReturn(stdout = stdout, stderr = stderr, error = null, errorCode = ShellErrorCode.ok)
//    }

    fun exec(commandList : MutableList<String>): ShellReturn {
        pb.command(commandList)
        ps = pb.start()
        pid = ps.pid()
        ps.waitFor()
//        ps.destroyForcibly()
        var stdout : String?
        var stderr : String?
        var errorCode : ShellErrorCode
        var error : String?

        try {
            stdout = BufferedReader(InputStreamReader(ps.inputStream)).readText()
        } catch (e : IOException) {
            stdout = null
            error = "stdout stream closed"
            errorCode = ShellErrorCode.stdoutStreamClosed
        }

        try {
            stderr = BufferedReader(InputStreamReader(ps.errorStream)).readText()
        } catch (e : IOException) {
            stderr = null
            error = "stderr strem closed"
            errorCode = ShellErrorCode.stderrStreamClosed
        }

        return ShellReturn(
            stdout = stdout,
            stderr = stderr
        )
    }

    fun destroy() : Boolean {
        if(isAction()) {
            ps.destroy()
            return true
        } else {
            return false
        }
    }

    fun isAction() : Boolean {
        return ps.isAlive
    }

    fun getPid(): Long? {
        return pid
    }
}

/***
 * shell 返回数据类
 */
data class ShellReturn(
    val stdout : String?,
    val stderr : String?,
    val error : String? = null,
    val errorCode : ShellErrorCode = ShellErrorCode.ok
)

/**
 * 错误枚举类型
 */
enum class ShellErrorCode {
    ok,
    stdoutStreamClosed,
    stderrStreamClosed
}