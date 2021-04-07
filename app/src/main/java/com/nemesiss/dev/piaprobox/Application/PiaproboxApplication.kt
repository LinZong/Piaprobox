package com.nemesiss.dev.piaprobox.Application

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.Persistence
import com.zzm.android_basic_library.CUGEAndroidSDK
import com.zzm.android_basic_library.util.CryptoUtil
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import me.weishu.reflection.Reflection
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

class PiaproboxApplication : Application() {
    private lateinit var crashLog: Logger
    override fun onCreate() {
        super.onCreate()
        Self = this
        crashLog = LoggerFactory.getLogger("crash")

        Persistence.Init(applicationContext)
        trustAllCertificates()
        setCrashLogHandler()
        scope.launch {
            CUGEAndroidSDK.init(
                applicationContext, 13,
                CryptoUtil.loadPubKeyFromImage(applicationContext, R.raw.piaprobox_demo), "piaprobox_demo"
            )
        }
    }

    companion object {
        val scope = MainScope()
        lateinit var Self: PiaproboxApplication
            private set
        init {
            System.setProperty("project.name", "Piaprobox")
            System.setProperty("log.platform", "ANDROID")
            System.setProperty("log.debug", "false")
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Reflection.unseal(base)
    }

    private fun trustAllCertificates() {
        val sslContext = SSLContext.getInstance("TLS")
        val trustManager: X509TrustManager = object : X509TrustManager {
            @SuppressLint("TrustAllX509TrustManager")
            @Throws(CertificateException::class)
            override fun checkClientTrusted(
                x509Certificates: Array<X509Certificate>,
                s: String
            ) {
            }

            @SuppressLint("TrustAllX509TrustManager")
            @Throws(CertificateException::class)
            override fun checkServerTrusted(
                x509Certificates: Array<X509Certificate>,
                s: String
            ) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return emptyArray()
            }
        }
        sslContext.init(
            null, arrayOf(
                trustManager
            ), SecureRandom()
        )
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
        HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
    }

    private fun setCrashLogHandler() {
        val defaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            crashLog.error("Crash detected!  \n", e)
            defaultCrashHandler.uncaughtException(t, e)
        }
        crashLog.info("Crash log handler is initialized!")
    }

    override fun onTerminate() {
        super.onTerminate()
        CUGEAndroidSDK.destroy()
        scope.cancel()
    }
}