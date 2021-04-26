package com.nemesiss.dev.piaprobox.util

import android.app.Activity

inline fun <reified T> Activity.getSystemService(): T {
    return getSystemService(T::class.java)
}