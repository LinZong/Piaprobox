package com.nemesiss.dev.piaprobox.Application

import android.app.Application
import com.nemesiss.dev.piaprobox.Service.AsyncExecutor

class PiaproboxApplication : Application() {
    val delayTaskExecutor = AsyncExecutor()
    override fun onCreate() {
        super.onCreate()
        Self = this
    }
    companion object {
        lateinit var Self : PiaproboxApplication
    }

}