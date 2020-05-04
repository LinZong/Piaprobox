package com.nemesiss.dev.piaprobox.Util

import android.view.View


fun <T : View> List<T>.whenClicks(vararg clickHandler: (View) -> Unit) {
    zip(clickHandler)
        .forEach { (view, listener) -> view.setOnClickListener { listener(it) } }
}

fun <T : View> List<T>.whenClicks(vararg clickHandler: View.OnClickListener) {
    zip(clickHandler)
        .forEach { (view, listener) -> view.setOnClickListener(listener) }
}