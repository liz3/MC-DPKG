package de.liz3.mcdpkg

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.channels.Channels
import javax.net.ssl.HttpsURLConnection

class Http {

    fun getRequest(lnk: String, headers: Map<String, String>, callback: (HttpResult) -> Unit) {
        val thread = Thread(Runnable {
            val connection = {
                if (lnk.startsWith("https://")) {
                    URL(lnk).openConnection() as HttpsURLConnection
                } else {
                    URL(lnk).openConnection() as HttpURLConnection
                }
            }.invoke()
            connection.requestMethod = "GET"
            connection.doInput = true


            for ((key, value) in headers) {
                connection.addRequestProperty(key, value)
            }
            connection.connect()
            callback.invoke(HttpResult(connection.responseCode, connection.inputStream, connection))
        })
        thread.name = "MCDpkg HTTP Thread"
        thread.start()
    }

    fun downloadFile(url:String, file:File) {

        val link = URL(url)
        val channel = Channels.newChannel(link.openStream())
        val outStream = FileOutputStream(file)
        outStream.channel.transferFrom(channel,0, Long.MAX_VALUE)
    }

    fun postRequest(lnk: String, content: String, headers: Map<String, String>, callback: (HttpResult) -> Unit) {
        val thread = Thread(Runnable {

            try {
                val connection = URL(lnk).openConnection() as HttpURLConnection
                connection.setRequestMethod("POST");
                for ((key, value) in headers) {
                    connection.addRequestProperty(key, value)
                }
                connection.doInput = true
                connection.doOutput = true
                connection.outputStream.write(content.toByteArray())
                callback.invoke(HttpResult(connection.responseCode, connection.inputStream, connection))
            }catch (e:Exception) {
                e.printStackTrace()
            }

        })
        thread.name = "MCDpkg HTTP Thread"
        thread.start()



    }

}

class HttpResult(val responseCode: Int, val inputStream: InputStream?, request: HttpURLConnection)