package com.nemesiss.dev.piaprobox.Application

import android.app.Application
import com.nemesiss.dev.piaprobox.Service.Persistence
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

class PiaproboxApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Self = this
        Persistence.Init(applicationContext)
        TrustAllCetificates()
    }

    companion object {
        lateinit var Self: PiaproboxApplication
    }


    private fun TrustAllCetificates() {
        val sslContext = SSLContext.getInstance("TLS")
        val trustManager: X509TrustManager = object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(
                x509Certificates: Array<X509Certificate>,
                s: String
            ) {
            }

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
}