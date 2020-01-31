package com.nemesiss.dev.piaprobox.Activity.Music

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.nemesiss.dev.piaprobox.Model.MusicPlayerActivityStatus
import com.nemesiss.dev.piaprobox.Model.MusicStatus
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.MusicPlayer.MusicPlayerServiceController
import com.nemesiss.dev.piaprobox.Service.MusicPlayer.MusicPlayerService
import com.nemesiss.dev.piaprobox.Service.MusicPlayer.SimpleMusicPlayer
import kotlinx.android.synthetic.main.music_player_layout.*


class MusicControlActivity : MusicPlayerActivity() {

    private var FROM_NOTIFICATION_INTENT = false
    private var SHOULD_PAUSE_MUSIC = false

    private var PendingPrepareURL = ""

    private var PlayerService: MusicPlayerService? = null
    private var PlayerServiceController: MusicPlayerServiceController? = null

    private var CurrentMusicTotalDuration = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        InitView()
        val status = intent.getSerializableExtra(PERSIST_STATUS_INTENT_KEY)
        StartService(NoNeedToStart = status != null)
//        SubscribeServiceStatus()
        if (status != null) {
            FROM_NOTIFICATION_INTENT = true
            SHOULD_PAUSE_MUSIC = false
            val activityStatus = status as MusicPlayerActivityStatus
            CurrentMusicTotalDuration = activityStatus.currentPlayMusicDuration
        } else {
            FROM_NOTIFICATION_INTENT = false
            val MusicContentUrl = intent.getStringExtra(MUSIC_CONTENT_URL) ?: ""
            if (MusicContentUrl.isNotEmpty() && !FROM_NOTIFICATION_INTENT) {
                SHOULD_PAUSE_MUSIC = true
            }
        }
    }

    private val PlayerServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(componentName: ComponentName?) {
            Log.d("MusicControlActivity", "音乐播放器服务取消绑定.")
            PlayerService = null
            PlayerServiceController = null
        }

        override fun onServiceConnected(componentName: ComponentName?, service: IBinder?) {
            PlayerService = (service as MusicPlayerService.MusicPlayerBinder).GetService()
            PlayerService?.NotifyServiceIsOK()
            Log.d("MusicControlActivity", "音乐播放器服务成功启动  ${PlayerService?.hashCode()}")
            PlayerServiceController = PlayerService!!.ServiceController
            if (SHOULD_PAUSE_MUSIC) {
                PlayerServiceController?.Stop()
            }
            SubscribeMusicPlayerStatus()
        }
    }

    @Synchronized
    private fun HandlePendingPrepareURL() {
        if (PendingPrepareURL.isNotEmpty()) {
            PlayerServiceController?.PrepareAsync(PendingPrepareURL, CurrentContentInfo!!)
            PlayerServiceController?.Play()
            PendingPrepareURL = ""
        }
    }

    @SuppressLint("CheckResult")
    private fun SubscribeServiceStatus() {
        MusicPlayerService.SERVICE_AVAILABLE.subscribe {
            if (it) {
                HandlePendingPrepareURL()
            }
        }
    }

    private fun PersistMusicPlayerActivityStatus(playerStatus: MusicStatus) {

        if (relatedMusicListData != null && lyricListData != null && CurrentContentInfo != null) {
            val StatusModel = MusicPlayerActivityStatus(
                relatedMusicListData!!,
                lyricListData!!,
                CurrentPlayMusicUrl,
                CurrentContentInfo!!,
                CurrentMusicTotalDuration
            )
            PlayerService?.UpdateWakeupMusicPlayerActivityIntent(StatusModel, playerStatus)
        }
    }


    @SuppressLint("CheckResult")
    private fun SubscribeMusicPlayerStatus() {
        PlayerServiceController?.PrepareStatus()
            ?.subscribe { status ->
                when (status) {
                    SimpleMusicPlayer.PrepareStatus.Prepared -> {
                        Log.d("MusicControlActivity", "音乐完成Prepare.")
                        CurrentMusicTotalDuration = PlayerService?.InnerPlayer?.GetDuration() ?: 0
                        MusicPlayer_TotalTime.text = Duration2Time(CurrentMusicTotalDuration)
//                        PersistMusicPlayerActivityStatus(MusicStatus.STOP)
                        if (PlayerServiceController?.PlayerStatus()?.value != MusicStatus.PLAY && SHOULD_PAUSE_MUSIC) {
                            PlayerServiceController?.Play()
                        }
                    }
                    SimpleMusicPlayer.PrepareStatus.Failed -> {
                        ResetTimeIndicator()
                    }
                    SimpleMusicPlayer.PrepareStatus.Destroyed -> {
                        ResetTimeIndicator()
                    }
                    SimpleMusicPlayer.PrepareStatus.Default -> {
                        ResetTimeIndicator()
                    }
                }
            }

        PlayerServiceController?.PlayerStatus()
            ?.subscribe { status ->
                when (status) {
                    MusicStatus.PLAY -> {
                        PersistMusicPlayerActivityStatus(MusicStatus.PLAY)
                        MusicPlayer_Control_Play.setImageResource(R.drawable.ic_pause_red_600_24dp)
                    }
                    MusicStatus.PAUSE -> {
                        PersistMusicPlayerActivityStatus(MusicStatus.PAUSE)
                        MusicPlayer_Control_Play.setImageResource(R.drawable.ic_play_arrow_red_600_24dp)
                    }
                    MusicStatus.STOP -> {
//                        MusicPlayer_Control_Play.setImageResource(R.drawable.ic_play_arrow_red_600_24dp)
                        ResetTimeIndicator()
                    }

                }
            }

        PlayerServiceController?.SetElapsedTimeListener { CurrentTimeStamp ->
            MusicPlayer_CurrentTime.text = Duration2Time(CurrentTimeStamp)
            MusicPlayer_Seekbar.progress = if (CurrentMusicTotalDuration == 0) {
                0
            } else {
                100 * CurrentTimeStamp / CurrentMusicTotalDuration
            }
        }

        PlayerServiceController?.SetBufferingListener {
            MusicPlayer_Seekbar.secondaryProgress = it
        }
    }

    private fun InitView() {
        MusicPlayer_Control_Play.setOnClickListener {
            if (MusicPlayerService.SERVICE_AVAILABLE.value == true) {
                when (PlayerServiceController?.Status()) {
                    MusicStatus.PLAY -> {
                        PlayerServiceController?.Pause()
                    }
                    MusicStatus.PAUSE -> {
                        PlayerServiceController?.Play()
                    }
                    MusicStatus.STOP -> {
                        PlayNewMusic()
                    }
                }
            } else {
                PendingPrepareURL = CurrentPlayMusicUrl
                StartService()
            }
        }
        MusicPlayer_Control_MoreInfo.setOnClickListener {

        }

        MusicPlayer_Control_Repeat.setOnClickListener {
        }
    }

    private fun ResetTimeIndicator() {
        CurrentMusicTotalDuration = 0
        MusicPlayer_Seekbar.progress = 0
        MusicPlayer_Seekbar.secondaryProgress = 0
        MusicPlayer_CurrentTime.text = Duration2Time(0)
        MusicPlayer_TotalTime.text = Duration2Time(0)
        MusicPlayer_Control_Play.setImageResource(R.drawable.ic_play_arrow_red_600_24dp)
        Log.d("MusicControlActivity", "重置图标完成.")
    }

    private fun PlayNewMusic() {
        ResetTimeIndicator()
        if (CurrentPlayMusicUrl.isNotEmpty()) {
            PlayerServiceController?.PrepareAsync(CurrentPlayMusicUrl, CurrentContentInfo!!)
            PersistMusicPlayerActivityStatus(MusicStatus.STOP)
            MusicPlayer_Control_Play.setImageResource(R.drawable.ic_more_horiz_red_600_24dp)
            return
        }
        // TODO Add Tips 播放器还没有准备好
    }

    private fun StartService(NoNeedToStart: Boolean = false) {
        val intent = Intent(this, MusicPlayerService::class.java)
        if (!NoNeedToStart) {
            startService(intent)
        }
        bindService(intent, PlayerServiceConnection, Context.BIND_AUTO_CREATE)
        SubscribeServiceStatus()
        SubscribeMusicPlayerStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MusicControlActivity", "MusicControlActivity unbindService onDestroy")

        if (PlayerServiceController?.PlayerStatus()?.value == MusicStatus.PLAY) {
            Log.d("MusicControlActivity", "应该保存当前Activity的状态信息.")
        }

        unbindService(PlayerServiceConnection)
    }

    // HELPER

    private fun Duration2Time(duration: Int): String? {
        val min = duration / 1000 / 60
        val sec = duration / 1000 % 60
        return (if (min < 10) "0$min" else min.toString() + "") + ":" + if (sec < 10) "0$sec" else sec.toString() + ""
    }

    private fun Progress2Duration(progress: Int, totalDuration: Int): Int {
        return (progress.toFloat() / 100 * totalDuration).toInt()
    }

}