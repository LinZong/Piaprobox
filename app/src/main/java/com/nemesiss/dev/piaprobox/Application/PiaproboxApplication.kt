package com.nemesiss.dev.piaprobox.Application

import android.app.Application
import android.content.Intent
import com.nemesiss.dev.piaprobox.Service.AsyncExecutor
import com.nemesiss.dev.piaprobox.Service.MusicPlayer.MusicPlayerService
import com.nemesiss.dev.piaprobox.Service.Persistence
import java.net.HttpURLConnection
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

class PiaproboxApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Self = this
        Persistence.Init(applicationContext)
    }
    companion object {
        lateinit var Self : PiaproboxApplication
    }
}