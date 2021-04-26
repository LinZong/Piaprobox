package com.nemesiss.dev.piaprobox.Service

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class AsyncExecutor {

    companion object {
        @JvmStatic
        private val DELAY_TASK_MAIN_MESSAGE: Int = 1

        @JvmStatic
        private val LOG_TAG = "AsyncExecutorLog"

        val INSTANCE: AsyncExecutor = AsyncExecutor()
    }

    private val mainThreadHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message?) {
            if (msg?.obj is Runnable) {
                val func = msg.obj as Runnable
                func.run()
            } else {
                Log.e(LOG_TAG, "Message object is not a Runnable!")
            }
        }
    }
    private val taskPool: ThreadPoolExecutor

    constructor() {
        // 默认线程池， CoreSize是CPU Size，最大是CPU+16。CallerRuns
        val cpuCores = Runtime.getRuntime().availableProcessors()
        taskPool = ThreadPoolExecutor(
            cpuCores,  // Initial pool size
            cpuCores + 16,       // Max pool size
            15,
            TimeUnit.SECONDS,
            LinkedBlockingQueue<Runnable>(cpuCores),
            ThreadPoolExecutor.CallerRunsPolicy()
        )
    }

    constructor(corePoolSize: Int, maxPoolSize: Int) {
        // 自定义线程池， 保证MaxSize比Core大，队列长度 CorePoolSize
        val maxSize = maxPoolSize.coerceAtLeast(corePoolSize + 1)
        taskPool = ThreadPoolExecutor(
            corePoolSize,       // Initial pool size
            maxSize,       // Max pool size
            15,
            TimeUnit.SECONDS,
            LinkedBlockingQueue<Runnable>(corePoolSize),
            ThreadPoolExecutor.CallerRunsPolicy()
        )
    }

    fun SendTask(Task: () -> Unit) {
        taskPool.execute(Task)
    }

    fun <T> SendTaskWithResult(Task: () -> T): Future<T> = taskPool.submit(Task)

    fun SendTaskMainThread(Task: Runnable) {
        val message = Message()
        message.what = DELAY_TASK_MAIN_MESSAGE
        message.obj = Task
        mainThreadHandler.sendMessage(message)
    }

    fun SendTaskMainThreadDelay(Task: Runnable, Delay: Long) {
        val message = Message()
        message.what = DELAY_TASK_MAIN_MESSAGE
        message.obj = Task
        mainThreadHandler.sendMessageDelayed(message, Delay)
    }

    fun shutdown() {
        taskPool.shutdown()
    }

    fun shutdownNow() {
        taskPool.shutdownNow()
    }
}