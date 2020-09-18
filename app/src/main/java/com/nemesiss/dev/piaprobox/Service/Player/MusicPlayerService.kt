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
import com.nemesiss.dev.piaprobox.Application.PiaproboxApplication
import com.nemesiss.dev.piaprobox.Model.MusicNotificationModel
import com.nemesiss.dev.piaprobox.Model.MusicPlayerActivityStatus
import com.nemesiss.dev.piaprobox.Model.MusicStatus
import com.nemesiss.dev.piaprobox.Service.Player.Legacy.SimpleMusicPlayer
import com.nemesiss.dev.piaprobox.Service.Player.NewPlayer.MusicPlayer
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
    private var WillPlayMusicURLFromActivity = ""

    override fun onCreate() {
        super.onCreate()
        musicPlayerNotificationManager =
            MusicPlayerNotificationManager(
                PiaproboxApplication.Self.applicationContext,
                Intent(this, MusicControlActivity::class.java)
            )
    }

    var player : MusicPlayer = SimpleMusicPlayerImpl(this)

    // Service内部使用
    private fun UpdateNotification(playerStatus: MusicStatus, playingMusicContentInfo: MusicContentInfo) {
        if (!IS_FOREGROUND) {
            ActAsForegroundService(
                musicPlayerNotificationManager.SendNotification(
                    MusicNotificationModel(
                        playingMusicContentInfo.Title
                        , playingMusicContentInfo.Artist, playerStatus
                    )
                    , true
                )
            )
        } else {
            musicPlayerNotificationManager.SendNotification(
                MusicNotificationModel(
                    playingMusicContentInfo.Title
                    , playingMusicContentInfo.Artist, playerStatus
                )
                , false
            )
        }
    }

    // Activity 主动调用的.
    fun UpdateWakeupMusicPlayerActivityIntent(musicPlayerActivityStatus: MusicPlayerActivityStatus) {
        val intent = Intent(PiaproboxApplication.Self.applicationContext, MusicControlActivity::class.java)
        intent.putExtra(MusicPlayerActivity.PERSIST_STATUS_INTENT_KEY, musicPlayerActivityStatus)
        musicPlayerNotificationManager.activityIntent = intent

        // 不需要再从Activity主动触发通知更新

        Log.d("MusicPlayerService", "已更新重入Activity的信息.")
    }

    inner class MusicPlayerBinder : Binder() {
        fun getService(): MusicPlayerService = this@MusicPlayerService
    }

    private fun ActAsForegroundService(noti: Notification) {
        IS_FOREGROUND = true
        startForeground(MusicPlayerNotificationManager.NotificationID, noti)
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
                GracefullyShutdown()
            }
            "PLAY" -> {
                if (WillPlayMusicURLFromActivity.isNotEmpty()) {
                    player.play(Uri.parse(WillPlayMusicURLFromActivity))
                } else {
                    player.resume()
                }
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
                    WillPlayMusicURLFromActivity = intent.getStringExtra("WillPlayMusicURL")
                    player.play(Uri.parse(WillPlayMusicURLFromActivity))
                }
            }
        }
        return START_STICKY
    }

    fun GracefullyShutdown() {
        SERVICE_AVAILABLE.onNext(false)
        player.destroy()
        stopForeground(true)
        IS_FOREGROUND = false
        musicPlayerNotificationManager.ClearNotification()
        MusicPlayerActivity.CleanStaticResources()
        Log.d("MusicPlayerService", "MusicPlayerService GracefullyShutdown")
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