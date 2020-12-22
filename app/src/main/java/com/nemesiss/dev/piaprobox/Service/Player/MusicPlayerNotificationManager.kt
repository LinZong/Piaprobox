package com.nemesiss.dev.piaprobox.Service.Player

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.nemesiss.dev.piaprobox.Activity.Common.MainActivity
import com.nemesiss.dev.piaprobox.Model.MusicNotificationModel
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.BaseNotificationManager
import com.nemesiss.dev.piaprobox.Service.Player.NewPlayer.PlayerAction

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

    fun prepareNotificationInstance(model: MusicNotificationModel): Notification {

        val normalNotificationRv = RemoteViews(context.packageName, R.layout.player_noti_normal)
        val bigNotificationRv = RemoteViews(context.packageName, R.layout.player_noti_big)

        normalNotificationRv.setTextViewText(R.id.MusicPlayer_Noti_SongName_Normal, model.SongName)
        normalNotificationRv.setTextViewText(R.id.MusicPlayer_Noti_SongArtist_Normal, model.ArtistName)

        bigNotificationRv.setTextViewText(R.id.MusicPlayer_Noti_SongName, model.SongName)
        bigNotificationRv.setTextViewText(R.id.MusicPlayer_Noti_SongArtist, model.ArtistName)


        val intentMaps = arrayOf("DESTROY", "PAUSE", "PLAY").associate { actionText ->
            Pair(
                actionText,
                PendingIntent.getService(
                    context,
                    55,
                    Intent(context, MusicPlayerService::class.java).apply { action = actionText },
                    0
                )
            )
        }

        when (model.CurrAction) {
            PlayerAction.PREPARING -> {
            }
            PlayerAction.PLAYING -> {
                bigNotificationRv.setImageViewResource(R.id.MusicPlayer_Noti_Play, R.drawable.ic_pause_red_600_24dp)
                bigNotificationRv.setOnClickPendingIntent(R.id.MusicPlayer_Noti_Play, intentMaps["PAUSE"])
            }
            PlayerAction.PAUSED -> {
                bigNotificationRv.setImageViewResource(
                    R.id.MusicPlayer_Noti_Play,
                    R.drawable.ic_play_arrow_red_600_24dp
                )
                bigNotificationRv.setOnClickPendingIntent(R.id.MusicPlayer_Noti_Play, intentMaps["PLAY"])
            }
            PlayerAction.STOPPED -> {
                bigNotificationRv.setImageViewResource(
                    R.id.MusicPlayer_Noti_Play,
                    R.drawable.ic_play_arrow_red_600_24dp
                )
                bigNotificationRv.setOnClickPendingIntent(R.id.MusicPlayer_Noti_Play, intentMaps["PLAY"])
            }
            PlayerAction.NO_ACTION -> {
            }
        }

        bigNotificationRv.setOnClickPendingIntent(R.id.MusicPlayer_NOti_Stop, intentMaps["DESTROY"])
        normalNotificationRv.setOnClickPendingIntent(R.id.MusicPlayer_NOti_Stop_Normal, intentMaps["DESTROY"])

        checkAndBuildChannel()


        activityIntent.action = Intent.ACTION_MAIN
        activityIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        val nextIntent = activityIntent.clone() as Intent
        val prevIntent = activityIntent.clone() as Intent

        nextIntent.action = "NEXT"
        prevIntent.action = "PREV"

        val ClickNotificationToOpenMusicPlayerIntents = arrayListOf(activityIntent)
        val NextMusicIntents = arrayListOf(nextIntent)
        val PrevMusicIntents = arrayListOf(prevIntent)


        val beginMainActivityIntent = Intent(context, MainActivity::class.java)
        beginMainActivityIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        beginMainActivityIntent.flags = beginMainActivityIntent.flags or Intent.FLAG_ACTIVITY_NEW_TASK

        ClickNotificationToOpenMusicPlayerIntents.add(0, beginMainActivityIntent)
        NextMusicIntents.add(0, beginMainActivityIntent)
        PrevMusicIntents.add(0, beginMainActivityIntent)


        val OpenMusicPlayerActivityPendingContentIntent =
            PendingIntent.getActivities(
                context,
                0,
                ClickNotificationToOpenMusicPlayerIntents.toTypedArray(),
                PendingIntent.FLAG_UPDATE_CURRENT
            )


        val NextMusicPlayerActivityPendingContentIntent =
            PendingIntent.getActivities(context, 0, NextMusicIntents.toTypedArray(), PendingIntent.FLAG_UPDATE_CURRENT)

        val PrevMusicPlayerActivityPendingContentIntent =
            PendingIntent.getActivities(context, 0, PrevMusicIntents.toTypedArray(), PendingIntent.FLAG_UPDATE_CURRENT)


        bigNotificationRv.setOnClickPendingIntent(
            R.id.MusicPlayer_Noti_Next,
            NextMusicPlayerActivityPendingContentIntent
        )
        bigNotificationRv.setOnClickPendingIntent(
            R.id.MusicPlayer_Noti_Previous,
            PrevMusicPlayerActivityPendingContentIntent
        )

        val notification = getDefaultNotificationBuilder()
            .setContentIntent(OpenMusicPlayerActivityPendingContentIntent)
            .setCustomContentView(normalNotificationRv)
            .setCustomBigContentView(bigNotificationRv)
            .build()

//        notification.contentView = normalNotificationRv
//        notification.cu = bigNotificationRv
        notification.flags = notification.flags or Notification.FLAG_ONGOING_EVENT or Notification.FLAG_NO_CLEAR
        notification.`when` = System.currentTimeMillis()
        return notification
    }
}