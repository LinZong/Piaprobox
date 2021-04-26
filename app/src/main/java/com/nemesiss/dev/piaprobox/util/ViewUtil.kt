package com.nemesiss.dev.piaprobox.util

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView

fun ViewGroup.enableAutoHideSoftKeyboard() {
    isFocusable = true
    isFocusableInTouchMode = true
    isClickable = true

    /**
     * 当某个View的isClickable设为true, 会使得这个View拉起一个CLICKABLE的Flag。
     * 有了CLICKABLE的Flag之后，这个View在接到onTouchEvent的时候才能
     * focusTaken = requestFocus();
     * 这个View的requestFocus会使得上一个持有交点的控件 TextInputEditText 接收到失去焦点的事件。
     * 进而回调到onFocusChangedListener，调起imm把输入法关掉。
     */
}

fun EditText.hideSoftKeyboard(imm: InputMethodManager) {
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun TextView.enableAutoHideSoftKeyboard(imm: InputMethodManager) {
    setOnFocusChangeListener { v, hasFocus ->
        if (!hasFocus && v is EditText) {
            v.hideSoftKeyboard(imm)
        }
    }
}

inline fun <reified T : ViewGroup> Activity.getRootLayout(): T {
    val view = findViewById<View>(android.R.id.content)
    if (view is T) {
        return view
    }
    if (view !is ViewGroup) {
        throw IllegalStateException("The root view of an activity must be a ViewGroup!")
    }
    return view.getChildAt(0) as T
}