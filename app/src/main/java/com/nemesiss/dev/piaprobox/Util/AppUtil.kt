package com.nemesiss.dev.piaprobox.Util

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Build
import android.os.Message
import android.support.v4.content.ContextCompat
import android.view.View
import com.karumi.dexter.Dexter
import com.karumi.dexter.DexterBuilder
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import com.nemesiss.dev.piaprobox.Activity.Common.PiaproboxBaseActivity
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

class PermissionUtil {
    companion object {
        @JvmStatic
        fun CheckStoragePermission(
            activity: PiaproboxBaseActivity,
            handler: MultiplePermissionsListener
        ) {
            Dexter.withActivity(activity)
                .withPermissions(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .withListener(handler)
                .check()
        }

        @JvmStatic
        fun ShowExplainDialog(
            context: Context,
            Title: String,
            Message: String,
            OkHandler: () -> Unit,
            RejectHandler: () -> Unit
        ) {
            AlertDialog
                .Builder(context)
                .setTitle(Title)
                .setMessage(Message)
                .setPositiveButton("Try again") { _,_ -> OkHandler() }
                .setNegativeButton("Cancel") {_,_ -> RejectHandler() }
                .show()
        }
    }
}

fun AssetManager.AsPath(FileName: String) = "file:///android_asset/$FileName"