package me.sagiri.mirai.plugin.QShell

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Image
import java.io.File
import java.util.*
import java.util.regex.Pattern
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


/**
 * 格式化图片
 */
class UploadImage {
    companion object {
        val client = HttpClient(OkHttp)

        /**
         * 格式化 msg 里面的 [qshell:image:http?...] 替换成Mirar image
         */
        suspend fun push(event: MessageEvent, msg: String): String {
            var newMsg = msg
            val m = Pattern.compile("\\[qshell:image:(http.+?)\\]").matcher(msg)
            val httpImageJob = QShellScope.launch {
                while (m.find()) {
                    val url = m.group(1)
                    val image = uploadImage(event, url)
                    if (image != null) {
                        newMsg = newMsg.replace(m.group(), "[mirai:image:${image.imageId}]")
                    }
                }
            }

            val fileImageJob = QShellScope.launch {
                val matcher = Pattern.compile("\\[qshell:image:file://(.+?)\\]").matcher(msg)
                while (matcher.find()) {
//                val job = QShellScope.launch {
                    val filePath = matcher.group(1)
                    val imageFile = File(filePath)
                    if (imageFile.exists()) {
                        val image = event.sender.uploadImage(imageFile)
                        newMsg = newMsg.replace(matcher.group(), "[mirai:image:${image.imageId}]")
                    }
                }
            }
//                jobs.add(job)
//            }

            val jobs = mutableListOf<Job>()
            jobs.map { job ->
                job.join()
            }

            fileImageJob.join()
            httpImageJob.join()

            return newMsg
        }


        /**
         * 通过http协议获取图片然后上传
         */
        private suspend fun uploadImage(event: MessageEvent, url: String): Image? {
            var format = "png"
            val matcher = Pattern.compile("\\.(\\w+?)$").matcher(url)
            if(matcher.find()) {
                format = matcher.group(1)
            }
            val response = client.get<HttpResponse>(url) {
                onDownload { bytesSentTotal, contentLength ->
                    Main.logger.info("${bytesSentTotal} / ${contentLength}")
                }
            }
            if(response.status == HttpStatusCode.OK) {
                val dir = File(System.getProperty("user.dir") + "/data/QShell/download")
                if (!dir.exists()) dir.mkdirs()
                val uuid = UUID.randomUUID()
                val file = File("$dir/${uuid}.${format}")
                file.writeBytes(response.readBytes())
                return event.sender.uploadImage(file)
            } else {
                return null
            }

            /**
            val req = HttpRequest.get(url)

            req.trustAllCerts()
            req.trustAllHosts()

            req.connectTimeout(10000)
            req.readTimeout(30000)

            var code = 0

            try {
                code = req.code()
            } catch (e: SocketTimeoutException) {

                return null
            }

            if (code == 200) {
                val dir = File(System.getProperty("user.dir") + "/data/QShell/download")
                if (!dir.exists()) dir.mkdirs()
                val file = File("$dir/tmp.png")
                req.receive(file)
                return event.sender.uploadImage(file, formatName = "png")
            } else {

                return null
            }

            */
        }
    }
}

object QShellScope : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = EmptyCoroutineContext

}