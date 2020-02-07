package com.nemesiss.dev.piaprobox.Util

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Build
import android.support.v4.content.ContextCompat
import android.view.View
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
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

        @JvmStatic
        fun IsAppRunning(context: Context, packageName: String): Boolean {
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val procInfos = activityManager.runningAppProcesses
            if (procInfos != null) {
                for (processInfo in procInfos) {
                    if (processInfo.processName == packageName) {
                        return true
                    }
                }
            }
            return false
        }

        @JvmStatic
        fun IsServiceRunning(context: Context, serviceClazz : Class<*>): Boolean {
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val serviceInfos = activityManager.getRunningServices(20)
            if (serviceInfos != null) {
                for(serv in serviceInfos) {
                    if(serv.service.className == serviceClazz.name) {
                        return true
                    }
                }
            }
            return false
        }

        @JvmStatic
        fun IsActivityAlivInTaskStack(context: Context, clazz: Class<*>) : Boolean {
            val intent = Intent(context, clazz)
            val componentName = intent.resolveActivity(context.packageManager)
            var flag = false
            if(componentName != null) {
                val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val taskLists = activityManager.getRunningTasks(20)
                for(task in taskLists) {
                    if(task.baseActivity == componentName) {
                        flag = true
                        break
                    }
                }
            }
            return flag
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