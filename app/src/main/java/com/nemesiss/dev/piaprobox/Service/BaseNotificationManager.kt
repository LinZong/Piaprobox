package com.nemesiss.dev.piaprobox.Service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.Player.MusicPlayerNotificationManager

open class BaseNotificationManager(val context: Context) {
    var notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager



    protected fun getDefaultNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, MusicPlayerNotificationManager.ChannelID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setVibrate(null).apply {
                priority = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                    NotificationManager.IMPORTANCE_MAX
                } else {
                    Notification.PRIORITY_MAX
                }
            }
    }

    protected fun checkAndBuildChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(MusicPlayerNotificationManager.ChannelID) == null) {
                val mChannel = NotificationChannel(
                    MusicPlayerNotificationManager.ChannelID, MusicPlayerNotificationManager.ChannelName, NotificationManager.IMPORTANCE_MIN
                )
                notificationManager.createNotificationChannel(mChannel)
            }
        }
    }
}