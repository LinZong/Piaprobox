package com.nemesiss.dev.piaprobox.Service.SimpleHTTP

import okhttp3.Response
import java.io.InputStream
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class SimpleResponseHandler<T : Any>(val response: Response, private val responseType: KClass<T>) {
    fun Handle(resolve: (T) -> Unit, rejected: (Int, Response) -> Unit) {
        if (response.isSuccessful) {
            resolve(
                when (responseType) {
                    String::class -> {
                        response.body?.string() as T
                    }
                    InputStream::class -> {
                        response.body?.byteStream() as T
                    }
                    else -> throw NotSupportDataTypeException("Cannot handle type: ${responseType.simpleName}")
                }
            )
        } else {
            rejected(response.code, response)
        }
    }
}

class NotSupportDataTypeException(reason: String) : IllegalArgumentException(reason)

inline fun <reified T> Response.handle(resolve: (T) -> Unit, rejected: (Int, Response) -> Unit) {
    if (body != null && isSuccessful) {
        when (T::class) {
            String::class -> resolve(body!!.string() as T)
            InputStream::class -> resolve(body!!.byteStream() as T)
            else -> throw NotSupportDataTypeException("Cannot handle type: ${T::class.simpleName}")
        }
    } else {
        rejected(code, this)
    }
}