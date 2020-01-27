package com.nemesiss.dev.piaprobox.Util

import android.app.Activity
import android.content.res.AssetManager
import android.os.Build
import android.support.v4.content.ContextCompat
import android.view.View
import com.nemesiss.dev.piaprobox.Application.PiaproboxApplication


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


        fun GetAppCachePath() : String {
            return ContextCompat.getExternalCacheDirs(PiaproboxApplication.Self.applicationContext)[0].absolutePath
        }
    }
}

fun AssetManager.AsPath(FileName : String) = "file:///android_asset/$FileName"