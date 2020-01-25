package com.nemesiss.dev.piaprobox.Util

import android.app.Activity
import android.os.Build
import android.view.View


class AppUtil {
    companion object {

        fun HideNavigationBar(activity: Activity) {
            if (Build.VERSION.SDK_INT < 19) {
                val v = activity.window.decorView
                v.systemUiVisibility = View.GONE
            } else {
                val decorView = activity.window.decorView
                val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN)
                decorView.systemUiVisibility = uiOptions
            }
        }

    }
}
