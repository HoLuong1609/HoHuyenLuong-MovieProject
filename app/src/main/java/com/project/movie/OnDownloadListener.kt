package com.project.movie

interface OnDownloadListener {

    fun onStartDownload()
    fun onProgress(downloadedBytes: Int)
    fun onFailed(e: Exception?)
    fun onDownloadCompleted(path: String)
}