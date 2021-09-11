package me.sagiri.mirai.plugin.QShell

import com.github.kevinsawicki.http.HttpRequest
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Image
import java.io.File
import java.net.SocketTimeoutException
import java.util.regex.Pattern


/**
 * 格式化图片
 */
class UploadImage {
    companion object {
        /**
         * 格式化 msg 里面的 [qshell:image:http?...] 替换成Mirar image
         */
        suspend fun push(event: MessageEvent, msg: String): String {
            var newMsg = msg
            val m = Pattern.compile("\\[qshell:image:(http.+?)\\]").matcher(msg)
            while (m.find()) {
                val url = m.group(1)
                val image = uploadImage(event, url)
                if (image != null) {
                    newMsg = newMsg.replace(m.group(), "[mirai:image:${image.imageId}]")
                }
            }

            return newMsg
        }

        /**
         * 通过http协议获取图片然后上传
         */
        private suspend fun uploadImage(event: MessageEvent, url: String): Image? {
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
        }
    }
}
