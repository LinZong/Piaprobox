package com.nemesiss.dev.piaprobox.Service.GlobalErrorHandler

import android.content.Context
import android.widget.Toast
import com.nemesiss.dev.piaprobox.Service.AsyncExecutor
import javax.inject.Inject
import javax.inject.Qualifier

class ParseContentErrorToastHandler @Inject constructor(val context: Context) : ParseContentErrorHandler() {

    private var asyncExecutor: AsyncExecutor = AsyncExecutor.INSTANCE

    override fun Output(code: Int, message: String) {
        asyncExecutor.SendTaskMainThread(Runnable {
            Toast.makeText(context, "$message  ($code)", Toast.LENGTH_SHORT).show()
        })
    }

    @Qualifier
    @Retention
    annotation class ToastHandler {}
}