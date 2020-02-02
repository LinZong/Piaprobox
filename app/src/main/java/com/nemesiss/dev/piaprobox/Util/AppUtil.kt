package com.nemesiss.dev.piaprobox.Util

import android.app.Activity
import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Build
import android.support.v4.content.ContextCompat
import android.view.View
import com.nemesiss.dev.piaprobox.Application.PiaproboxApplication


class AppUtil {
    companion object {

        @JvmStatic
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

        @JvmStatic
        fun GetAppCachePath(): String {
            return ContextCompat.getExternalCacheDirs(PiaproboxApplication.Self.applicationContext)[0].absolutePath
        }

        @JvmStatic
        fun Dp2Px(resources: Resources, dp: Int): Int {
            val scale = resources.displayMetrics.density
            return (dp * scale + 0.5).toInt()
        }

        @JvmStatic
        fun Px2Dp(resources: Resources, px: Int): Int {
            val scale = resources.displayMetrics.density
            return (px / scale + 0.5).toInt()
        }
    }


}

fun AssetManager.AsPath(FileName: String) = "file:///android_asset/$FileName"