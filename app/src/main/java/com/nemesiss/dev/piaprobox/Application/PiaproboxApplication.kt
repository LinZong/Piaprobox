package com.nemesiss.dev.piaprobox.Application

import android.app.Application
import android.content.Intent
import com.nemesiss.dev.piaprobox.Service.AsyncExecutor
import com.nemesiss.dev.piaprobox.Service.MusicPlayer.MusicPlayerService
import com.nemesiss.dev.piaprobox.Service.Persistence

class PiaproboxApplication : Application() {
    val delayTaskExecutor = AsyncExecutor()
    override fun onCreate() {
        super.onCreate()
        Self = this
        Persistence.Init(applicationContext)
    }
    companion object {
        lateinit var Self : PiaproboxApplication
    }

    override fun onTerminate() {
        super.onTerminate()
    }
}