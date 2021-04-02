package me.sagiri.mirai.plugin.QShell

import okhttp3.internal.wait
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

    fun exec(cmd : String): String? {
        pb.command(cmd)
        val ps = pb.start()
        val stdlog = BufferedReader(InputStreamReader(ps.inputStream)).readText()
        val stderr = BufferedReader(InputStreamReader(ps.errorStream)).readText()

        if (stdlog != "" ) {
            return stdlog
        } else if( stderr != "") {
            return stderr
        } else {
            return null
        }
    }

    fun exec(commandList : MutableList<String>): String? {
        pb.command(commandList)
        ps = pb.start()
        pid = ps.pid()
        ps.waitFor()
//        ps.destroyForcibly()
        var stdlog = ""
        var stderr = ""
        try {

            stdlog = BufferedReader(InputStreamReader(ps.inputStream)).readText()
            stderr = BufferedReader(InputStreamReader(ps.errorStream)).readText()
        } catch (e : IOException) {

        }

        if (stdlog != "" ) {
            return stdlog
        } else if( stderr != "") {
            return stderr
        } else {
            return null
        }
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