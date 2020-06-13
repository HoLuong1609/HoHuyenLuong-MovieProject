package com.project.movie

import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.webkit.URLUtil
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class DownloadImageThread(
    private val urlStr: String,
    private val downloadListener: OnDownloadListener
) :
    Runnable {
    override fun run() {
        var fos: FileOutputStream? = null
        var inputStream: BufferedInputStream? = null
        try {
            updateStatus { downloadListener.onStartDownload() }
            val url = URL(urlStr)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            var totalDataRead = 0f
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
                    var i: Int
                    while (`in`.read(data, 0, 1024).also { i = it } >= 0) {
                        totalDataRead += i
                        updateStatus { downloadListener.onProgress(totalDataRead.toInt()) }
                        bout.write(data, 0, i)
                    }
                    updateStatus { downloadListener.onCompleted(file.path) }
                }
            }
        } catch (ex: Exception) {
            updateStatus { downloadListener.onFailed(ex) }
        } finally {
            inputStream?.close()
            fos?.close()
        }
    }

    private fun updateStatus(function: () -> Unit) {
        Handler(Looper.getMainLooper()).post { function.invoke() }
    }

    private fun getFileName(url: String): String {
        var fileName = ""
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

    interface OnDownloadListener {
        fun onStartDownload()
        fun onProgress(downloadedKb: Int)
        fun onCompleted(filePath: String)
        fun onFailed(e: Exception?)
    }
}