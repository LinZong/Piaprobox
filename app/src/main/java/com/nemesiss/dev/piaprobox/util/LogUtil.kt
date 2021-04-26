package com.nemesiss.dev.piaprobox.util

import android.widget.Toast
import com.nemesiss.dev.piaprobox.Application.PiaproboxApplication
import org.slf4j.Logger

fun String.showToast() {
    Toast.makeText(PiaproboxApplication.Self.applicationContext, this, Toast.LENGTH_SHORT).show()
}

fun Logger.errorWithToast(msg: String) {
    msg.showToast()
    error(msg)
}

fun Logger.errorWithToast(msg: String, throwable: Throwable) {
    "$msg ${throwable.message ?: ""}".showToast()
    error(msg, throwable)
}

fun Logger.infoWithToast(msg: String) {
    msg.showToast()
    info(msg)
}

fun Logger.infoWithToast(msg: String, throwable: Throwable) {
    "$msg ${throwable.message ?: ""}".showToast()
    info(msg, throwable)
}

fun Logger.warnWithToast(msg: String) {
    msg.showToast()
    warn(msg)
}

fun Logger.warnWithToast(msg: String, throwable: Throwable) {
    "$msg ${throwable.message ?: ""}".showToast()
    warn(msg, throwable)
}
