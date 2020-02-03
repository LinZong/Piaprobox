package com.nemesiss.dev.piaprobox.Service.Download

import android.app.Notification
import android.content.Context
import com.nemesiss.dev.piaprobox.Service.BaseNotificationManager
import com.nemesiss.dev.piaprobox.Service.MusicPlayer.MusicPlayerNotificationManager

class DownloadNotificationManager(context: Context) : BaseNotificationManager(context) {

    companion object {

        @JvmStatic
        val DownloadNotificationID = 393940

        @JvmStatic
        val ChannelName = "PiaproBox MusicPlayer Notification Channel"

        @JvmStatic
        val ChannelID = "piaprobox-noti-1"
    }


    fun SendDownloadNotification(fileName: String, progress: Int) {
        val downloadProgress = GetDefualtNotificationBuilder()
            .setAutoCancel(true)
            .setContentTitle("Downloading $fileName")
            .setProgress(100, progress, false)
            .build()
        downloadProgress.flags = (downloadProgress.flags).or(Notification.FLAG_ONGOING_EVENT)
        notificationManager.notify(MusicPlayerNotificationManager.DownloadNotificationID, downloadProgress)
    }

    fun SendDownloadFinishNotification(fileName: String) {
        val downloadProgress = GetDefualtNotificationBuilder()
            .setAutoCancel(true)
            .setContentTitle("Download $fileName finished!")
            .build()
        downloadProgress.flags = (downloadProgress.flags).or(Notification.FLAG_AUTO_CANCEL)
        notificationManager.notify(MusicPlayerNotificationManager.DownloadNotificationID, downloadProgress)
    }

    fun SendDownloadFailedNotification(fileName: String) {
        val downloadProgress = GetDefualtNotificationBuilder()
            .setAutoCancel(true)
            .setContentTitle("Download $fileName failed!")
            .build()
        downloadProgress.flags = (downloadProgress.flags).or(Notification.FLAG_AUTO_CANCEL)
        notificationManager.notify(MusicPlayerNotificationManager.DownloadNotificationID, downloadProgress)
    }


}