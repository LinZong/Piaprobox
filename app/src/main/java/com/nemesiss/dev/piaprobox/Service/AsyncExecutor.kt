package com.nemesiss.dev.piaprobox.Service

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import dagger.Component
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

class AsyncExecutor @Inject constructor() {

    companion object {
        @JvmStatic
        private val DELAY_TASK_MAIN_MESSAGE : Int = 1

        @JvmStatic
        private val LOG_TAG = "AsyncExecutorLog"
    }

    private val mainThreadHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message?) {
            if (msg?.obj is Runnable) {
                val func = msg.obj as Runnable
                func.run()
            }
            else {
                Log.e(LOG_TAG, "Message object is not a Runnable!")
            }
        }
    }
    private val asyncTaskQueue: BlockingQueue<Runnable> = LinkedBlockingQueue<Runnable>()
    private val InnerTaskThreadPool = ThreadPoolExecutor(
        Runtime.getRuntime().availableProcessors(),       // Initial pool size
        16,       // Max pool size
        1,
        TimeUnit.SECONDS,
        asyncTaskQueue
    )

    fun SendTask(Task : () -> Unit) {
        InnerTaskThreadPool.execute(Task)
    }

    fun SendTaskMainThread(Task :Runnable) {
        val message = Message()
        message.what = DELAY_TASK_MAIN_MESSAGE
        message.obj = Task
        mainThreadHandler.sendMessage(message)
    }
    fun SendTaskMainThreadDelay(Task: Runnable, Delay : Long) {
        val message = Message()
        message.what = DELAY_TASK_MAIN_MESSAGE
        message.obj = Task
        mainThreadHandler.sendMessageDelayed(message, Delay)
    }
}