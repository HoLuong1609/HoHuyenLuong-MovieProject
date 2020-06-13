package com.project.movie

import android.app.Application

class MovieApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        HOLDER.INSTANCE = this
    }

    private object HOLDER {
        lateinit var INSTANCE: MovieApplication
    }

    companion object {
        val instance: MovieApplication by lazy { HOLDER.INSTANCE }
    }
}