package com.nemesiss.dev.piaprobox.Service.MusicPlayer

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.nemesiss.dev.HTMLContentParser.Model.MusicContentInfo
import com.nemesiss.dev.piaprobox.Activity.Music.MusicControlActivity
import com.nemesiss.dev.piaprobox.Activity.Music.MusicPlayerActivity
import com.nemesiss.dev.piaprobox.Model.MusicNotificationModel
import com.nemesiss.dev.piaprobox.Model.MusicPlayerActivityStatus
import com.nemesiss.dev.piaprobox.Model.MusicStatus
import io.reactivex.subjects.BehaviorSubject

class MusicPlayerService : Service() {

    companion object {
        @JvmStatic
        var SERVICE_AVAILABLE = BehaviorSubject.createDefault(false)
    }

    private var IS_BINDED = false
    private var IS_FOREGROUND = false

    private lateinit var musicPlayerNotificationManager: MusicPlayerNotificationManager
    private var PlayingMusicContentInfo: MusicContentInfo? = null
    private var WillPlayMusicURLFromActivity = ""

    override fun onCreate() {
        super.onCreate()
        musicPlayerNotificationManager =
            MusicPlayerNotificationManager(this, Intent(this, MusicControlActivity::class.java))
    }

    var InnerPlayer: SimpleMusicPlayer? = null
        get() {
            if (field == null) {
                field = SimpleMusicPlayer(this)
            }
            return field
        }
        private set

    val ServiceController = object :
        MusicPlayerServiceController {
        override fun PrepareAsync(URL: String, musicContent: MusicContentInfo) {
            PlayingMusicContentInfo = musicContent
            WillPlayMusicURLFromActivity = "" // Clear pending prepare.
            UpdateNotification(MusicStatus.STOP, musicContent)
            InnerPlayer!!.LoadMusic(URL)
        }

        override fun Loop(isLoop: Boolean) {
            InnerPlayer!!.SetLooping(isLoop)
        }

        override fun Play() {
            InnerPlayer!!.Play(false)
            UpdateNotification(MusicStatus.PLAY, PlayingMusicContentInfo!!)
        }

        override fun Pause() {
            InnerPlayer!!.Pause()
            UpdateNotification(MusicStatus.PAUSE, PlayingMusicContentInfo!!)
        }

        override fun Stop() {
            UpdateNotification(MusicStatus.STOP, PlayingMusicContentInfo!!)
            InnerPlayer!!.Stop()
        }

        override fun SeekTo(progress: Int) {
            InnerPlayer!!.SeekTo(progress)
        }

        override fun SetElapsedTimeListener(listener: (Int) -> Unit) {
            InnerPlayer!!.TimeElapsedListener = SimpleMusicPlayer.OnPlayerTimeElapsedListener { listener(it) }
        }

        override fun SetBufferingListener(listener: (Int) -> Unit) {
            InnerPlayer!!.InnerMediaPlayer.setOnBufferingUpdateListener { _, percentage ->
                listener(percentage)
            }
        }

        override fun PlayerStatusValue(): MusicStatus {
            return InnerPlayer!!.MusicPlayStatus.value!!
        }

        override fun TestSendNotification(contentInfo: MusicContentInfo, currStatus: MusicStatus) {
            musicPlayerNotificationManager.SendNotification(
                MusicNotificationModel(
                    contentInfo.Title,
                    "测试歌手",
                    currStatus
                ), false
            )
        }

        override fun PrepareStatus(): BehaviorSubject<SimpleMusicPlayer.PrepareStatus>? {
            return InnerPlayer!!.IsPrepared
        }

        override fun PlayerStatus(): BehaviorSubject<MusicStatus>? {
            return InnerPlayer!!.MusicPlayStatus
        }

        override fun DisableElapsedTimeDispatcher() {
            InnerPlayer!!.DisableDispatchElapsedTimeStamp()
        }

        override fun EnabletElapsedTimeDispatcher() {
            InnerPlayer!!.BeginDispatchElapsedTimeStamp()
        }
    }

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

    fun UpdateWakeupMusicPlayerActivityIntent(
        musicPlayerActivityStatus: MusicPlayerActivityStatus, playerStatus: MusicStatus
    ) {
        val intent: Intent
        intent = Intent(this, MusicControlActivity::class.java)
        intent.putExtra(MusicPlayerActivity.PERSIST_STATUS_INTENT_KEY, musicPlayerActivityStatus)
        musicPlayerNotificationManager.activityIntent = intent
        musicPlayerNotificationManager.SendNotification(
            MusicNotificationModel(
                musicPlayerActivityStatus.currentPlayMusicContentInfo.Title,
                musicPlayerActivityStatus.currentPlayMusicContentInfo.Artist,
                playerStatus
            ), false
        )
        Log.d("MusicPlayerService", "已更新重入Activity的信息.")
    }

    inner class MusicPlayerBinder : Binder() {
        fun GetService(): MusicPlayerService {
            return this@MusicPlayerService
        }
    }

    private fun ActAsForegroundService(noti: Notification) {
        IS_FOREGROUND = true
        startForeground(MusicPlayerNotificationManager.NotificationID, noti)
    }

    override fun onBind(p0: Intent?): IBinder? {
        Log.d("MusicPlayerService", "初次启动, 对象HashCode: ${this.hashCode()}")
        IS_BINDED = true
        SERVICE_AVAILABLE.onNext(true)
        return MusicPlayerBinder()
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        IS_BINDED = true
    }

    override fun onUnbind(intent: Intent?): Boolean {
        SERVICE_AVAILABLE.onNext(false)
        IS_BINDED = false
        return super.onUnbind(intent)
    }

    fun NotifyServiceIsOK() {
        IS_BINDED = true
        SERVICE_AVAILABLE.onNext(true)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d("MusicPlayerService", "即将onStartCommand, 对象HashCode: ${this.hashCode()}")
        if (IS_BINDED && !SERVICE_AVAILABLE.value!!) {
            SERVICE_AVAILABLE.onNext(true)
        }
        when (intent?.action) {
            "DESTORY" -> {
                Log.d("MusicPlayerService", "即将停止, 对象HashCode: ${this.hashCode()}")
                GracefullyShutdown()
            }
            "PLAY" -> {
                if (WillPlayMusicURLFromActivity.isNotEmpty()) {
                    ServiceController.PrepareAsync(WillPlayMusicURLFromActivity,PlayingMusicContentInfo!!)
                }
                ServiceController.Play()
            }
            "PAUSE" -> {
                ServiceController.Pause()
            }
            "STOP" -> {
                ServiceController.Stop()
            }
            "UPDATE_INFO" -> {
                PlayingMusicContentInfo = intent.getSerializableExtra("UpdateMusicContentInfo") as MusicContentInfo?
                if (PlayingMusicContentInfo != null) {
                    ServiceController.Stop()
//                    UpdateNotification(MusicStatus.STOP, PlayingMusicContentInfo!!)
                    WillPlayMusicURLFromActivity = intent.getStringExtra("WillPlayMusicURL")
                }
            }
        }

        return START_STICKY
    }

    fun GracefullyShutdown() {
        SERVICE_AVAILABLE.onNext(false)
        InnerPlayer!!.Stop()
        stopForeground(true)
        IS_FOREGROUND = false
        musicPlayerNotificationManager.ClearNotification()
        Log.d("MusicPlayerService", "MusicPlayerService GracefullyShutdown")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MusicPlayerService", "MusicPlayerService onDestroy")
        InnerPlayer!!.SafetyDestroy()
        InnerPlayer = null
        stopForeground(true)
        IS_FOREGROUND = false
        musicPlayerNotificationManager.ClearNotification()
        stopSelf()
//        stopService(Intent(this, MusicPlayerService::class.java))
    }
}

interface MusicPlayerServiceController {
    fun PrepareAsync(URL: String, musicContent: MusicContentInfo)
    fun Loop(isLoop: Boolean)
    fun Play()
    fun Pause()
    fun Stop()
    fun SeekTo(progress : Int)
    fun SetElapsedTimeListener(listener: (Int) -> Unit)
    fun SetBufferingListener(listener: (Int) -> Unit)
    fun PlayerStatusValue(): MusicStatus
    fun TestSendNotification(contentInfo: MusicContentInfo, currStatus: MusicStatus)
    fun PrepareStatus(): BehaviorSubject<SimpleMusicPlayer.PrepareStatus>?
    fun PlayerStatus(): BehaviorSubject<MusicStatus>?
    fun DisableElapsedTimeDispatcher()
    fun EnabletElapsedTimeDispatcher()
}
