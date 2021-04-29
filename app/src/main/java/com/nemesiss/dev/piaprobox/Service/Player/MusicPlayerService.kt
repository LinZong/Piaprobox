package com.nemesiss.dev.piaprobox.Service.Player

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.nemesiss.dev.contentparser.model.MusicContentInfo
import com.nemesiss.dev.piaprobox.Activity.Music.MusicControlActivity
import com.nemesiss.dev.piaprobox.Activity.Music.MusicPlayerActivity
import com.nemesiss.dev.piaprobox.Activity.Music.MusicPlayerActivity.Companion.PERSIST_STATUS_INTENT_KEY
import com.nemesiss.dev.piaprobox.Application.PiaproboxApplication
import com.nemesiss.dev.piaprobox.model.MusicNotificationModel
import com.nemesiss.dev.piaprobox.model.MusicPlayerActivityStatus
import com.nemesiss.dev.piaprobox.Service.Player.MusicPlayerNotificationManager.Companion.NotificationID
import com.nemesiss.dev.piaprobox.Service.Player.NewPlayer.DefaultMusicPlayerStateChangedCallback
import com.nemesiss.dev.piaprobox.Service.Player.NewPlayer.MusicPlayer
import com.nemesiss.dev.piaprobox.Service.Player.NewPlayer.PlayerAction
import com.nemesiss.dev.piaprobox.Service.Player.NewPlayer.impl.SimpleMusicPlayerImpl
import io.reactivex.subjects.BehaviorSubject

class MusicPlayerService : Service() {

    companion object {
        @JvmStatic
        var SERVICE_AVAILABLE = BehaviorSubject.createDefault(false)

        @JvmStatic
        var IS_BINDED = false
            private set

        @JvmStatic
        var IS_FOREGROUND = false
            private set
    }

    private lateinit var musicPlayerNotificationManager: MusicPlayerNotificationManager
    private var PlayingMusicContentInfo: MusicContentInfo? = null

    lateinit var player: MusicPlayer
        private set

    override fun onCreate() {
        super.onCreate()
        player = SimpleMusicPlayerImpl(this)

        player.registerStateChangedListener(object : DefaultMusicPlayerStateChangedCallback() {
            private fun triggerNotificationUpdate() {
                UpdateNotification(player.state(), PlayingMusicContentInfo!!)
            }
            override fun onPlaying(player: MusicPlayer) {
                triggerNotificationUpdate()
            }

            override fun onPausing(player: MusicPlayer) {
                triggerNotificationUpdate()
            }

            override fun onStopping(player: MusicPlayer) {
                triggerNotificationUpdate()
            }
        })

        musicPlayerNotificationManager =
            MusicPlayerNotificationManager(
                PiaproboxApplication.Self.applicationContext,
                Intent(this, MusicControlActivity::class.java)
            )
        setServiceAvailable(true)
    }




    // Service内部使用
    private fun UpdateNotification(playerAction: PlayerAction, playingMusicContentInfo: MusicContentInfo) {

        val pendingNewNotification = musicPlayerNotificationManager.prepareNotificationInstance(
            MusicNotificationModel(
                playingMusicContentInfo.Title
                , playingMusicContentInfo.Artist, playerAction
            )
        )
        if (!IS_FOREGROUND) {
            bringToForegroundService(pendingNewNotification)
        } else {
            musicPlayerNotificationManager.notificationManager.notify(NotificationID, pendingNewNotification)
        }
    }

    // Activity 主动调用的.
    fun UpdateWakeupMusicPlayerActivityIntent(musicPlayerActivityStatus: MusicPlayerActivityStatus) {
        val intent = Intent(PiaproboxApplication.Self.applicationContext, MusicControlActivity::class.java)
        intent.putExtra(PERSIST_STATUS_INTENT_KEY, musicPlayerActivityStatus)
        musicPlayerNotificationManager.activityIntent = intent
        PlayingMusicContentInfo?.let { contentInfo ->
            UpdateNotification(
                player.state(),
                contentInfo
            )
        }
    }

    inner class MusicPlayerBinder : Binder() {
        fun getService(): MusicPlayerService = this@MusicPlayerService
    }

    private fun bringToForegroundService(notification: Notification) {
        IS_FOREGROUND = true
        startForeground(NotificationID, notification)
    }

    override fun onBind(p0: Intent?): IBinder {
        IS_BINDED = true
        return MusicPlayerBinder()
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        IS_BINDED = true
    }

    override fun onUnbind(intent: Intent?): Boolean {
        IS_BINDED = false
        return super.onUnbind(intent)
    }

    private fun setServiceAvailable(available: Boolean) {
        SERVICE_AVAILABLE.onNext(available)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            "DESTROY" -> {
                stopPlaying()
            }
            "PLAY" -> {
                player.resume()
            }
            "PAUSE" -> {
                player.pause()
            }
            "STOP" -> {
                player.stop()
            }
            "UPDATE_INFO" -> {
                setServiceAvailable(true)
                PlayingMusicContentInfo = intent.getSerializableExtra("UpdateMusicContentInfo") as MusicContentInfo?
                if (PlayingMusicContentInfo != null) {
                    player.stop()
                    player.play(Uri.parse(intent.getStringExtra("WillPlayMusicURL")))
                }
            }
        }
        return START_STICKY
    }

    private fun stopPlaying() {
//        setServiceAvailable(false)
        player.stop()
        stopForeground(true)
        IS_FOREGROUND = false
        musicPlayerNotificationManager.ClearNotification()
        Log.d("MusicPlayerService", "MusicPlayerService stopPlaying")
    }

    override fun onDestroy() {
        setServiceAvailable(false)
        Log.d("MusicPlayerService", "MusicPlayerService onDestroy")
        IS_BINDED = false
        player.destroy()
        stopForeground(true)
        IS_FOREGROUND = false
        musicPlayerNotificationManager.ClearNotification()
        stopSelf()
        MusicPlayerActivity.CleanStaticResources()
        super.onDestroy()
    }
}