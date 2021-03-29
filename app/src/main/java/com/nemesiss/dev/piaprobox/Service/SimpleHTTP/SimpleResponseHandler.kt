package com.nemesiss.dev.piaprobox.Service.SimpleHTTP

import okhttp3.Response
import java.io.InputStream
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
@Deprecated("Java世界里面泛型擦除而无法得到运行时泛型的垃圾设计是时候被扫进垃圾桶了。",
    replaceWith = ReplaceWith("请使用Kotlin的内联具体化参数版本的Response<reified T>.handle()代替。"))
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