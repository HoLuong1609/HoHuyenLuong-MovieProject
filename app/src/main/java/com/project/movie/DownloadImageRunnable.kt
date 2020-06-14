package com.project.movie

import android.os.Environment
import android.text.TextUtils
import android.webkit.URLUtil
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class DownloadImageRunnable(
    private val urlStr: String,
    private val downloadListener: OnDownloadListener
) : Runnable {
    override fun run() {
        var filePath = ""
        var fos: FileOutputStream? = null
        var inputStream: BufferedInputStream? = null
        try {
            val url = URL(urlStr)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            inputStream = BufferedInputStream(connection.inputStream)
            inputStream.use { `in` ->
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                if (!dir.exists()) {
                    dir.mkdir()
                }
                val file = File(dir, getFileName(urlStr))
                fos = FileOutputStream(file)
                BufferedOutputStream(fos!!, 1024).use { bout ->
                    val data = ByteArray(1024)
                    var read: Int
                    while (`in`.read(data, 0, 1024).also { read = it } >= 0) {
                        downloadListener.onProgress(read)
                        bout.write(data, 0, read)
                    }
                    filePath = file.path
                }
            }
        } catch (ex: Exception) {
            downloadListener.onFailed(ex)
        } finally {
            inputStream?.close()
            fos?.close()
        }
        downloadListener.onDownloadCompleted(filePath)
    }

    private fun getFileName(url: String): String {
        var fileName: String
        val calendar = Calendar.getInstance()
        val currentTime = calendar.timeInMillis
        try {
            fileName = calendar.get(Calendar.MILLISECOND).toString() + URLUtil.guessFileName(url, null, null)
            if (TextUtils.isEmpty(fileName)) {
                fileName = "$currentTime.jpg"
            }
        } catch (e: java.lang.Exception) {
            fileName = "$currentTime.jpg"
        }
        return fileName
    }
}