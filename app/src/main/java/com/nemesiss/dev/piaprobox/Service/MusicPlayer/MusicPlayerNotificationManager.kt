package com.nemesiss.dev.piaprobox.Service.MusicPlayer

import android.app.*
import android.content.Context
import android.content.Intent

import android.widget.RemoteViews
import com.nemesiss.dev.piaprobox.Activity.Common.MainActivity
import com.nemesiss.dev.piaprobox.Model.MusicNotificationModel
import com.nemesiss.dev.piaprobox.Model.MusicStatus
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.BaseNotificationManager
import com.nemesiss.dev.piaprobox.Util.AppUtil

class MusicPlayerNotificationManager(context: Context, var activityIntent: Intent) : BaseNotificationManager(context) {

    companion object {

        @JvmStatic
        val DownloadNotificationID = 393940

        @JvmStatic
        val NotificationID = 393939

        @JvmStatic
        val ChannelName = "PiaproBox MusicPlayer Notification Channel"


        @JvmStatic
        val ChannelID = "piaprobox-noti-1"
    }


    fun ClearNotification() {
        notificationManager.cancel(NotificationID)
    }

    fun SendNotification(model: MusicNotificationModel, butDontSend: Boolean = true): Notification {

        val NormalNotiView = RemoteViews(context.packageName, R.layout.player_noti_normal)
        val BigNotiView = RemoteViews(context.packageName, R.layout.player_noti_big)

        NormalNotiView.setTextViewText(R.id.MusicPlayer_Noti_SongName_Normal, model.SongName)
        NormalNotiView.setTextViewText(R.id.MusicPlayer_Noti_SongArtist_Normal, model.ArtistName)

        BigNotiView.setTextViewText(R.id.MusicPlayer_Noti_SongName, model.SongName)
        BigNotiView.setTextViewText(R.id.MusicPlayer_Noti_SongArtist, model.ArtistName)

        val CloseServiceIntent = Intent(context, MusicPlayerService::class.java).apply {
            action = "DESTORY"
        }

        val CloseServicePendingIntent = PendingIntent.getService(context, 55, CloseServiceIntent, 0)


        val PauseIntent = Intent(context, MusicPlayerService::class.java)
            .apply { action = "PAUSE" }

        val PausePendingIntent = PendingIntent.getService(context, 55, PauseIntent, 0)

//        val NextIntent = Intent(context, MusicPlayerService::class.java)
//            .apply { action = "NEXT" }
//
//        val NextPendingIntent = PendingIntent.getService(context, 55, NextIntent, 0)
//
//        val PrevIntent = Intent(context, MusicPlayerService::class.java)
//            .apply { action = "PREV" }
//        val PrevPendingIntent = PendingIntent.getService(context, 55, PrevIntent, 0)

        val PlayIntent = Intent(context, MusicPlayerService::class.java)
            .apply { action = "PLAY" }

        val PlayPendingIntent = PendingIntent.getService(context, 55, PlayIntent, 0)

        when (model.CurrStatus) {
            MusicStatus.PLAY -> {
                BigNotiView.setImageViewResource(R.id.MusicPlayer_Noti_Play, R.drawable.ic_pause_red_600_24dp)
                BigNotiView.setOnClickPendingIntent(R.id.MusicPlayer_Noti_Play, PausePendingIntent)
            }
            MusicStatus.PAUSE -> {
                BigNotiView.setImageViewResource(R.id.MusicPlayer_Noti_Play, R.drawable.ic_play_arrow_red_600_24dp)
                BigNotiView.setOnClickPendingIntent(R.id.MusicPlayer_Noti_Play, PlayPendingIntent)
            }
            MusicStatus.STOP -> {
                BigNotiView.setImageViewResource(R.id.MusicPlayer_Noti_Play, R.drawable.ic_play_arrow_red_600_24dp)
                BigNotiView.setOnClickPendingIntent(R.id.MusicPlayer_Noti_Play, PlayPendingIntent)
            }
        }
        BigNotiView.setOnClickPendingIntent(R.id.MusicPlayer_NOti_Stop, CloseServicePendingIntent)
        NormalNotiView.setOnClickPendingIntent(R.id.MusicPlayer_NOti_Stop_Normal, CloseServicePendingIntent)





        CheckAndBuildChannel()



        activityIntent.action = Intent.ACTION_MAIN
        activityIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        val nextIntent = activityIntent.clone() as Intent
        val prevIntent = activityIntent.clone() as Intent

        nextIntent.action = "NEXT"
        prevIntent.action = "PREV"

        val ClickNotificationToOpenMusicPlayerIntents = arrayListOf(activityIntent)
        val NextMusicIntents = arrayListOf(nextIntent)
        val PrevMusicIntents = arrayListOf(prevIntent)


        // 判断如果此时App退出了，才重启MainActivity.

//        if (!AppUtil.IsActivityAlivInTaskStack(context, MainActivity::class.java)) {
            val beginMainActivityIntent = Intent(context, MainActivity::class.java)
            beginMainActivityIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            beginMainActivityIntent.flags = beginMainActivityIntent.flags or Intent.FLAG_ACTIVITY_NEW_TASK

            ClickNotificationToOpenMusicPlayerIntents.add(0,beginMainActivityIntent)
            NextMusicIntents.add(0,beginMainActivityIntent)
            PrevMusicIntents.add(0,beginMainActivityIntent)
//        }


        val OpenMusicPlayerActivityPendingContentIntent =
            PendingIntent.getActivities(context, 0, ClickNotificationToOpenMusicPlayerIntents.toTypedArray(), PendingIntent.FLAG_UPDATE_CURRENT)


        val NextMusicPlayerActivityPendingContentIntent =
            PendingIntent.getActivities(context, 0, NextMusicIntents.toTypedArray(), PendingIntent.FLAG_UPDATE_CURRENT)

        val PrevMusicPlayerActivityPendingContentIntent =
            PendingIntent.getActivities(context, 0, PrevMusicIntents.toTypedArray(), PendingIntent.FLAG_UPDATE_CURRENT)


        BigNotiView.setOnClickPendingIntent(R.id.MusicPlayer_Noti_Next, NextMusicPlayerActivityPendingContentIntent)
        BigNotiView.setOnClickPendingIntent(R.id.MusicPlayer_Noti_Previous, PrevMusicPlayerActivityPendingContentIntent)

        val notification = GetDefualtNotificationBuilder()
            .setContentIntent(OpenMusicPlayerActivityPendingContentIntent)
            .build()

        notification.contentView = NormalNotiView
        notification.bigContentView = BigNotiView
        notification.flags = notification.flags or Notification.FLAG_ONGOING_EVENT
        notification.`when` = System.currentTimeMillis()
        if (!butDontSend)
            notificationManager.notify(NotificationID, notification)
        return notification
    }
}