package com.nemesiss.dev.piaprobox.Activity

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.view.View
import android.widget.RemoteViews
import com.nemesiss.dev.piaprobox.Activity.Common.PiaproboxBaseActivity
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.MusicPlayer.MusicPlayerService


class TestSkeletonActivity : PiaproboxBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_skeleton)

    }

    fun SendNotification(view: View) {
        val notiView = RemoteViews(packageName, R.layout.player_noti_big)
        val notiViewNormal = RemoteViews(packageName,R.layout.player_noti_normal)

        val closeServiceIntent = Intent(this, MusicPlayerService::class.java)
        closeServiceIntent.action = "STOP"
        val closeIntent = PendingIntent.getService(this,55,closeServiceIntent,0)

        notiView.setOnClickPendingIntent(R.id.MusicPlayer_NOti_Stop, closeIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = 1
        val channelId = "piaprobox-noti-1"
        val channelName = "PiaproBox MusicPlayer Notification Channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            if(notificationManager.getNotificationChannel(channelId) == null) {
                val mChannel = NotificationChannel(
                    channelId, channelName, importance
                )
                notificationManager.createNotificationChannel(mChannel)
//            }
        }

//        val mBuilder = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.mipmap.ic_launcher)
//            .setContentTitle(title)
//            .setDefaults(Notification.DEFAULT_LIGHTS.or(Notification.DEFAULT_SOUND))
//            .setVibrate(null)

        val mBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setDefaults(Notification.DEFAULT_LIGHTS.or(Notification.DEFAULT_SOUND))
            .setPriority(Notification.PRIORITY_MAX)
            .setVibrate(null)

        val stackBuilder: TaskStackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addNextIntent(intent)
        val resultPendingIntent: PendingIntent = stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        mBuilder.setContentIntent(resultPendingIntent)
        val notification = mBuilder.build()
        notification.contentView = notiViewNormal
        notification.bigContentView = notiView
        notification.flags = (notification.flags).or(Notification.FLAG_ONGOING_EVENT)
        notification.`when` = System.currentTimeMillis()
        notificationManager.notify(notificationId, notification)

    }
}