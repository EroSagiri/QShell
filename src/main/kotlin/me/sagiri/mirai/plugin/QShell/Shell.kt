package me.sagiri.mirai.plugin.QShell

import java.io.BufferedReader
import java.io.InputStreamReader

class Shell {
    private lateinit var pb : ProcessBuilder

    init {
        pb = ProcessBuilder()
//        pb.directory(File(System.getenv("HOME")))
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
}