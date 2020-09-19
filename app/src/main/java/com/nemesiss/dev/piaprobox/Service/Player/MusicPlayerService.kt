package com.nemesiss.dev.piaprobox.Service.Player

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.nemesiss.dev.HTMLContentParser.Model.MusicContentInfo
import com.nemesiss.dev.piaprobox.Activity.Music.MusicControlActivity
import com.nemesiss.dev.piaprobox.Activity.Music.MusicPlayerActivity
import com.nemesiss.dev.piaprobox.Activity.Music.MusicPlayerActivity.Companion.PERSIST_STATUS_INTENT_KEY
import com.nemesiss.dev.piaprobox.Application.PiaproboxApplication
import com.nemesiss.dev.piaprobox.Model.MusicNotificationModel
import com.nemesiss.dev.piaprobox.Model.MusicPlayerActivityStatus
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

    override fun onCreate() {
        super.onCreate()
        musicPlayerNotificationManager =
            MusicPlayerNotificationManager(
                PiaproboxApplication.Self.applicationContext,
                Intent(this, MusicControlActivity::class.java)
            )
    }

    var player: MusicPlayer = SimpleMusicPlayerImpl(this)
        private set

    init {
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
            ActAsForegroundService(pendingNewNotification)
        } else {
            musicPlayerNotificationManager.notificationManager.notify(NotificationID, pendingNewNotification)
        }
    }

    // Activity 主动调用的.
    fun UpdateWakeupMusicPlayerActivityIntent(musicPlayerActivityStatus: MusicPlayerActivityStatus) {
        val intent = Intent(PiaproboxApplication.Self.applicationContext, MusicControlActivity::class.java)
        intent.putExtra(PERSIST_STATUS_INTENT_KEY, musicPlayerActivityStatus)
        musicPlayerNotificationManager.activityIntent = intent
        PlayingMusicContentInfo?.let { playingMusicContentInfo ->
            UpdateNotification(
                player.state(),
                playingMusicContentInfo
            )
        }
    }

    inner class MusicPlayerBinder : Binder() {
        fun getService(): MusicPlayerService = this@MusicPlayerService
    }

    private fun ActAsForegroundService(notification: Notification) {
        IS_FOREGROUND = true
        startForeground(NotificationID, notification)
    }

    override fun onBind(p0: Intent?): IBinder? {
        Log.d("MusicPlayerService", "初次启动, 对象HashCode: ${this.hashCode()}")
        NotifyServiceIsOK()
        return MusicPlayerBinder()
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        NotifyServiceIsOK()
        Log.d("MusicPlayerService", "重新绑定, 对象HashCode: ${this.hashCode()}")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        SERVICE_AVAILABLE.onNext(false)
        IS_BINDED = false
        return super.onUnbind(intent)
    }

    private fun NotifyServiceIsOK() {
        IS_BINDED = true
        SERVICE_AVAILABLE.onNext(true)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d("MusicPlayerService", "即将onStartCommand, 对象HashCode: ${this.hashCode()}")
        SERVICE_AVAILABLE.onNext(true)
        when (intent?.action) {
            "DESTROY" -> {
                Log.d("MusicPlayerService", "即将停止, 对象HashCode: ${this.hashCode()}")
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
        SERVICE_AVAILABLE.onNext(false)
        player.stop()
        stopForeground(true)
        IS_FOREGROUND = false
        musicPlayerNotificationManager.ClearNotification()
        MusicPlayerActivity.CleanStaticResources()
        Log.d("MusicPlayerService", "MusicPlayerService stopPlaying")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MusicPlayerService", "MusicPlayerService onDestroy")
        SERVICE_AVAILABLE.onNext(false)
        player.destroy()
        stopForeground(true)
        IS_FOREGROUND = false
        musicPlayerNotificationManager.ClearNotification()
        stopSelf()
        MusicPlayerActivity.CleanStaticResources()
    }
}