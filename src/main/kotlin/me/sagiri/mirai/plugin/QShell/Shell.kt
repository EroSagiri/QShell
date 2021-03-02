package me.sagiri.mirai.plugin.QShell

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class Shell {
    private lateinit var pb : ProcessBuilder
    var shell = "bash"
    var shell_arvs = "-c"

    init {
        pb = ProcessBuilder()
        pb.directory(File(System.getenv("HOME")))
    }

    fun exec(cmd : String): String? {
        val shell_cmd = listOf(shell, shell_arvs, cmd)
        pb.command(shell_cmd)
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

    fun start() : Boolean {

        return true
    }

    public  fun  stop() : Boolean {
        return  true
    }
}