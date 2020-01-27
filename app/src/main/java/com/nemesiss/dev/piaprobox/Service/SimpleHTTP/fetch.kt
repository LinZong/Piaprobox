package com.nemesiss.dev.piaprobox.Service.SimpleHTTP

import com.nemesiss.dev.piaprobox.Service.AsyncExecutor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject

class Fetch @Inject constructor(val httpClient: OkHttpClient, val asyncExecutor: AsyncExecutor) {
    var URL: String = ""

    fun visit(url: String): Fetch {
        URL = url
        return this
    }

    fun go(): Response {
        CheckParameter()
        return httpClient.newCall(Request.Builder().url(URL).get().build())
            .execute()
    }

    fun goAsync(resolve: (Response) -> Unit) {
        CheckParameter()
        asyncExecutor.SendTask {
            resolve(
                httpClient.newCall(Request.Builder().url(URL).get().build())
                    .execute()
            )
        }
    }

    private fun CheckParameter() {
        if (URL.isEmpty()) {
            throw IllegalArgumentException("URL Should not be empty!")
        }
    }
}