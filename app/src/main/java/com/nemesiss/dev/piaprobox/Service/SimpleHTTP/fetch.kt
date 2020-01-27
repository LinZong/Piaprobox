package com.nemesiss.dev.piaprobox.Service.SimpleHTTP

import com.nemesiss.dev.piaprobox.Service.AsyncExecutor
import com.nemesiss.dev.piaprobox.Service.DaggerModules.OkHttpModules
import dagger.Component
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.lang.Exception
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

    fun goAsync(resolve: (Response) -> Unit, unexpectedFail: (Exception) -> Unit) {
        CheckParameter()
        asyncExecutor.SendTask {
            try {
                resolve(
                    httpClient.newCall(Request.Builder().url(URL).get().build())
                        .execute()
                )
            } catch (ex: Exception) {
                unexpectedFail(ex)
            }
        }
    }

    private fun CheckParameter() {
        if (URL.isEmpty()) {
            throw IllegalArgumentException("URL Should not be empty!")
        }
    }
}


@Component(modules = [OkHttpModules::class])
interface FetchFactory {
    fun fetcher(): Fetch
}