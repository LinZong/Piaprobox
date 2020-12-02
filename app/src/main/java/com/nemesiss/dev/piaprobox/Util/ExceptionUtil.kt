package com.nemesiss.dev.piaprobox.Util

import java.util.concurrent.ExecutionException
import java.util.concurrent.Future

@Throws(ExecutionException::class)
fun <V> Future<V>.getOrException(): V {
    try {
        return get()
    } catch (e: ExecutionException) {
        throw e.cause ?: e
    }
}