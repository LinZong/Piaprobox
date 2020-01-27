package com.nemesiss.dev.piaprobox.Service.SimpleHTTP

import okhttp3.Response
import java.io.InputStream
import kotlin.reflect.KClass

class SimpleResponseHandler(val response: Response, val responseType : KClass<out Any>) {

    fun Handle(resolve : (Any)->Unit, rejected : (Int, Response) -> Unit) {
        if(response.isSuccessful) {
            resolve(when(responseType) {
                String::class -> {
                    response.body?.string() ?: ""
                }
                InputStream::class -> {
                    response.body?.byteStream()!!
                }
                else -> response.body?.string() ?: ""
            })
        } else {
            rejected(response.code, response)
        }
    }
}