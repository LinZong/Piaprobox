package com.nemesiss.dev.piaprobox.Service.SimpleHTTP

import okhttp3.Response
import java.io.InputStream
import kotlin.reflect.KClass

class SimpleResponseHandler<T : Any>(val response: Response, val responseType : KClass<out T>) {

    fun Handle(resolve : (T)->Unit, rejected : (Int, Response) -> Unit) {
        if(response.isSuccessful) {
            resolve(when(responseType) {
                String::class -> {
                    (response.body?.string() ?: "") as T
                }
                InputStream::class -> {
                    response.body?.byteStream()!! as T
                }
                else -> response.body?.string() as T
            })
        } else {
            rejected(response.code, response)
        }
    }
}