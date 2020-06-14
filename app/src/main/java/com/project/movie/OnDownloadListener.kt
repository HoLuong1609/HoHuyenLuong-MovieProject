package com.project.movie

interface OnDownloadListener {

    fun onStartDownload()
    fun onProgress(downloadedKb: Int)
    fun onFailed(e: Exception?)
}