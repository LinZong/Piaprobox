package com.nemesiss.dev.piaprobox.Service.SimpleHTTP


import com.nemesiss.dev.piaprobox.Service.AsyncExecutor
import com.nemesiss.dev.piaprobox.Service.DaggerModules.OkHttpModules
import com.nemesiss.dev.piaprobox.Service.Persistence
import dagger.Component
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject

class Fetch @Inject constructor(val httpClient: OkHttpClient) {

    private val asyncExecutor: AsyncExecutor = AsyncExecutor.INSTANCE
    var URL: String = ""
    var savedCookies: ArrayList<Pair<String, String>> = ArrayList()

    fun visit(url: String): Fetch {
        URL = url
        return this
    }

    fun go(): Response {
        val hds = Headers
            .Builder()
            .add("Cookie", savedCookies.joinToString(";") { "${it.first}=${it.second}" })
            .build()

        CheckParameter()
        return httpClient.newCall(Request.Builder().url(URL).headers(hds).get().build())
            .execute()
    }

    fun cookie(single: Pair<String,String>): Fetch {
        cookie(single.first, single.second)
        return this
    }

    fun cookie(name: String, value: String): Fetch {
        savedCookies.add(name to value)
        return this
    }

    fun cookies(vararg cookies: Pair<String, String>): Fetch {
        cookies.forEach { single -> cookie(single) }
        return this
    }

    fun withLoginCookie(): Fetch {
        Persistence.GetLoginCookie()?.apply {
            cookies(*pairs)
        }
        return this
    }

    fun goAsync(resolve: (Response) -> Unit, unexpectedFail: (Exception) -> Unit) {
        val hds = Headers
            .Builder()
            .add("Cookie", savedCookies.joinToString(";") { "${it.first}=${it.second}" })
            .build()

        CheckParameter()
        asyncExecutor.SendTask {
            try {
                resolve(
                    httpClient
                        .newCall(Request.Builder().url(URL).headers(hds).get().build())
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