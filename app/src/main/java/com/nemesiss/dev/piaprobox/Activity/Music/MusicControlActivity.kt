package com.nemesiss.dev.piaprobox.Activity.Music

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.SeekBar
import com.nemesiss.dev.piaprobox.Model.MusicPlayerActivityStatus
import com.nemesiss.dev.piaprobox.Model.MusicStatus
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.MusicPlayer.MusicPlayerServiceController
import com.nemesiss.dev.piaprobox.Service.MusicPlayer.MusicPlayerService
import com.nemesiss.dev.piaprobox.Service.MusicPlayer.SimpleMusicPlayer
import com.nemesiss.dev.piaprobox.Service.Persistence
import com.nemesiss.dev.piaprobox.View.Common.whenClicks
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.music_player_layout.*


class MusicControlActivity : MusicPlayerActivity() {

    private var FROM_NOTIFICATION_INTENT = false
    private var NEW_MUSIC_LOADED = false
    private var IS_SEEKING = false
    private var IS_ENABLE_LOOPING = false
        set(value) {
            PlayerServiceController?.Loop(value)
            Persistence.SetMusicPlayerLoopStatus(value)
            MusicPlayer_Control_Repeat.setImageResource(
                if (value)
                    R.drawable.ic_repeat_one_red_600_24dp
                else R.drawable.ic_repeat_red_600_24dp
            )
            field = value
        }

    private var PendingPrepareURL = ""
    private var PlayerService: MusicPlayerService? = null
    private var PlayerServiceController: MusicPlayerServiceController? = null
    private var CurrentMusicTotalDuration = 0
    private var SubscribedRelations = ArrayList<Disposable?>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        InitView()
        LoadUserPreferenceSetup()
        PrepareActivityStatus()
    }

    private fun LoadUserPreferenceSetup() {
        IS_ENABLE_LOOPING = Persistence.GetMusicPlayerLoopStatus()
    }

    private fun PrepareActivityStatus() {
        val status = intent.getSerializableExtra(PERSIST_STATUS_INTENT_KEY)
        StartService(NoNeedToStart = status != null)
        if (status != null) {
            // 从通知栏消息过来.
            FROM_NOTIFICATION_INTENT = true
            NEW_MUSIC_LOADED = false
            val activityStatus = status as MusicPlayerActivityStatus
            RecoverActivityStatusFromPersistObject(activityStatus)
        } else {
            FROM_NOTIFICATION_INTENT = false
            // 从点击RecommendItem过来
            val MusicContentUrl = intent.getStringExtra(MUSIC_CONTENT_URL) ?: ""
            if (LAST_MUSIC_PLAYER_ACTIVITY_STATUS != null) {
                // 恢复
                RecoverActivityStatusFromPersistObject(LAST_MUSIC_PLAYER_ACTIVITY_STATUS!!)
            } else if (MusicContentUrl.isNotEmpty()) {
                NEW_MUSIC_LOADED = true
            }
        }
    }

    private fun InitSeekbarController() {
        MusicPlayer_Seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && PlayerServiceController?.PlayerStatusValue() != MusicStatus.STOP) {
                    MusicPlayer_CurrentTime.text =
                        (Duration2Time(Progress2Duration(progress, CurrentMusicTotalDuration)))
                }
            }

            override fun onStartTrackingTouch(seekbar: SeekBar?) {
                IS_SEEKING = true
                PlayerServiceController?.DisableElapsedTimeDispatcher()
            }

            override fun onStopTrackingTouch(seekbar: SeekBar?) {
                IS_SEEKING = false
                PlayerServiceController?.SeekTo(seekbar!!.progress)
                PlayerServiceController?.EnabletElapsedTimeDispatcher()
            }
        })
    }

    private fun InitView() {
        listOf(
            MusicPlayer_Control_Play,
            MusicPlayer_Control_MoreInfo,
            MusicPlayer_Control_Repeat
        )
            .whenClicks({
                if (MusicPlayerService.SERVICE_AVAILABLE.value == true) {
                    when (PlayerServiceController?.PlayerStatusValue()) {
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
                    PendingPrepareURL = CurrentMusicPlayInfo?.URL ?: ""
                    StartService()
                }
            }, {
                val intent = Intent(this, MusicDetailActivity::class.java)
                intent.putExtra(MusicDetailActivity.MUSIC_CONTENT_INFO_INTENT_KEY, CurrentMusicContentInfo)
                startActivity(intent)
            }, {
                IS_ENABLE_LOOPING = !IS_ENABLE_LOOPING
            })
        InitSeekbarController()
    }


    private fun RecoverActivityStatusFromPersistObject(activityStatus: MusicPlayerActivityStatus) {

        Log.d("MusicControlActivity", "MusicControlActivity  开始恢复上一次的Activity状态")

        IS_ENABLE_LOOPING = activityStatus.openSingleLooping
        CurrentMusicTotalDuration = activityStatus.currentPlayMusicTotalDuration
        MusicPlayer_Seekbar.progress = activityStatus.currentPlayMusicElapsedDuration
        MusicPlayer_CurrentTime.text = Duration2Time(activityStatus.currentPlayMusicElapsedDuration)
        MusicPlayer_Seekbar.secondaryProgress = activityStatus.currentBufferDuration
    }

    private val PlayerServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(componentName: ComponentName?) {
            Log.d("MusicControlActivity", "音乐播放器服务取消绑定.")
            PlayerService = null
            PlayerServiceController = null
        }

        override fun onServiceConnected(componentName: ComponentName?, service: IBinder?) {
            PlayerService = (service as MusicPlayerService.MusicPlayerBinder).GetService()
            Log.d("MusicControlActivity", "音乐播放器服务Bind完成  ${PlayerService?.hashCode()}")
            PlayerServiceController = PlayerService!!.ServiceController
            SubscribeMusicPlayerStatus()
        }
    }

    private fun HandlePendingPrepareURL() {
        if (PendingPrepareURL.isNotEmpty()) {
            PlayerServiceController?.PrepareAsync(PendingPrepareURL, CurrentMusicContentInfo!!)
            PlayerServiceController?.Play()
            PendingPrepareURL = ""
        }
    }

    @SuppressLint("CheckResult")
    private fun SubscribeServiceStatus() {
        SubscribedRelations.add(MusicPlayerService.SERVICE_AVAILABLE.subscribe {
            if (it) {
                HandlePendingPrepareURL()
            }
        })
    }

    private fun CanPersistMusicPlayerActivityStatus() =
        relatedMusicListData != null &&
                lyricListData != null &&
                CurrentMusicContentInfo != null &&
                CurrentMusicPlayInfo != null

    fun PersistMusicPlayerActivityStatus(playerStatus: MusicStatus, AlsoUpdateActivityIntent: Boolean = true) {

        if (CanPersistMusicPlayerActivityStatus()) {
            val ActivityStatusModel = MusicPlayerActivityStatus(
                relatedMusicListData!!,
                lyricListData!!,
                CurrentMusicPlayInfo!!,
                CurrentMusicContentInfo!!,
                CurrentMusicTotalDuration,
                MusicPlayer_Seekbar.progress,
                MusicPlayer_Seekbar.secondaryProgress,
                IS_ENABLE_LOOPING
            )
            if (AlsoUpdateActivityIntent)
                PlayerService?.UpdateWakeupMusicPlayerActivityIntent(ActivityStatusModel)
            LAST_MUSIC_PLAYER_ACTIVITY_STATUS = ActivityStatusModel
            LAST_PLAYER_STATUS = playerStatus
        }
    }


    companion object {
        @JvmStatic
        private var LAST_PLAYER_STATUS: MusicStatus? = null

        @JvmStatic
        private fun Duration2Time(duration: Int): String? {
            val min = duration / 1000 / 60
            val sec = duration / 1000 % 60
            return (if (min < 10) "0$min" else min.toString() + "") + ":" + if (sec < 10) "0$sec" else sec.toString() + ""
        }

        @JvmStatic
        private fun Progress2Duration(progress: Int, totalDuration: Int): Int {
            return (progress.toFloat() / 100 * totalDuration).toInt()
        }
    }

    private val HAVE_A_PENDING_PLAY_REQ: Boolean
        get() = PlayerServiceController?.PlayerStatus()?.value == MusicStatus.STOP && NEW_MUSIC_LOADED

    @SuppressLint("CheckResult")
    private fun SubscribeMusicPlayerStatus() {
        SubscribedRelations.add(
            PlayerServiceController?.PrepareStatus()
                ?.subscribe { status ->
                    when (status) {
                        SimpleMusicPlayer.PrepareStatus.Prepared -> {
                            Log.d("MusicControlActivity", "音乐完成Prepare.")
                            CurrentMusicTotalDuration = PlayerService?.InnerPlayer?.GetDuration() ?: 0
                            MusicPlayer_TotalTime.text = Duration2Time(CurrentMusicTotalDuration)
                            if (HAVE_A_PENDING_PLAY_REQ) {
                                PlayerServiceController?.Play()
                            }
                            if (IS_ENABLE_LOOPING) {
                                PlayerServiceController?.Loop(true)
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
        )
        SubscribedRelations.add(
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
                            ResetTimeIndicator()
                        }
                        MusicStatus.END -> {
                            if (IS_ENABLE_LOOPING) {
                                PlayerServiceController?.Play()
                                PlayerServiceController?.Loop(true)
                            } else {
                                PlayerServiceController?.Stop()
                            }
                        }

                    }
                }
        )

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
        if (CurrentMusicPlayInfo != null) {
            PlayerServiceController?.PrepareAsync(CurrentMusicPlayInfo!!.URL, CurrentMusicContentInfo!!)
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

        if (PlayerServiceController?.PlayerStatusValue()!! != MusicStatus.STOP) {
            Log.d("MusicControlActivity", "应该保存当前Activity的状态信息.")
            PersistMusicPlayerActivityStatus(PlayerServiceController?.PlayerStatusValue()!!)
        }

        unbindService(PlayerServiceConnection)
        SubscribedRelations.forEach {
            if (it?.isDisposed != false)
                it?.dispose()
        }
    }
}