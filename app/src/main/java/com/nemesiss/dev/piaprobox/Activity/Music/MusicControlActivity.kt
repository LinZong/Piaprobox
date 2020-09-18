package com.nemesiss.dev.piaprobox.Activity.Music

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import android.widget.SeekBar
import com.nemesiss.dev.piaprobox.Model.MusicPlayerActivityStatus
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.Persistence
import com.nemesiss.dev.piaprobox.Service.Player.MusicPlayerService
import com.nemesiss.dev.piaprobox.Service.Player.NewPlayer.DefaultMusicPlayerStateChangedCallback
import com.nemesiss.dev.piaprobox.Service.Player.NewPlayer.MusicPlayer
import com.nemesiss.dev.piaprobox.Service.Player.NewPlayer.PlayerAction
import com.nemesiss.dev.piaprobox.View.Common.whenClicks
import kotlinx.android.synthetic.main.music_player_layout.*


class MusicControlActivity : MusicPlayerActivity() {

    private var FROM_NOTIFICATION_INTENT = false
    private var NEW_MUSIC_LOADED = false
    private var IS_SEEKING = false
    private var IS_ENABLE_LOOPING = false
        @Synchronized
        set(value) {
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
    private var player: MusicPlayer? = null
    private var CurrentMusicTotalDuration = 0

    private val timeElapsedUpdater = Handler(this::handleQueryTimeStamp)

    private fun handleQueryTimeStamp(message: Message): Boolean {
        MusicPlayer_CurrentTime.text = Duration2Time(CurrentMusicTotalDuration)
        player?.let { player ->
            MusicPlayer_Seekbar.progress = (player.currentTimestamp() * 100 / player.duration()).toInt()
        }
        timeElapsedUpdater.sendEmptyMessageDelayed(100, 200)
        return true
    }

    private fun beginQueryTimeStamp() {
        timeElapsedUpdater.sendEmptyMessage(100)
    }

    private fun stopQueryTimeStamp() {
        timeElapsedUpdater.removeMessages(100)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        InitView()
        LoadUserPreferenceSetup()
        PrepareActivityStatus()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        LoadUserPreferenceSetup()
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
                if (fromUser) {
                    MusicPlayer_CurrentTime.text =
                        (Duration2Time(Progress2Duration(progress, CurrentMusicTotalDuration)))
                }
            }
            override fun onStartTrackingTouch(seekbar: SeekBar?) {
                IS_SEEKING = true
            }

            override fun onStopTrackingTouch(seekbar: SeekBar?) {
                seekbar?.apply {
                    IS_SEEKING = false
                    player?.seekTo(progress)
                }
            }
        })
    }

    private fun InitView() {
        listOf(
            MusicPlayer_Control_Play,
            MusicPlayer_Control_MoreInfo,
            MusicPlayer_Control_Repeat,
            MusicPlayer_Control_Next,
            MusicPlayer_Control_Previous
        )
            .whenClicks({
                if (MusicPlayerService.SERVICE_AVAILABLE.value == true) {
                    when (player?.state()) {
                        PlayerAction.PREPARING -> {}
                        PlayerAction.PLAYING -> { player?.pause() }
                        PlayerAction.PAUSED -> { player?.resume() }
                        PlayerAction.STOPPED -> { player?.resume() }
                        PlayerAction.NO_ACTION -> {}
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
            }, {
                NextMusic()
            }, {
                PrevMusic()
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
        }

        override fun onServiceConnected(componentName: ComponentName?, service: IBinder?) {
            PlayerService = (service as MusicPlayerService.MusicPlayerBinder).getService()
            Log.d("MusicControlActivity", "音乐播放器服务Bind完成  ${PlayerService.hashCode()}")
            player = PlayerService!!.player
            SubscribeMusicPlayerStatus()
        }
    }

    private fun CanPersistMusicPlayerActivityStatus() =
        relatedMusicListData != null &&
                lyricListData != null &&
                CurrentMusicContentInfo != null &&
                CurrentMusicPlayInfo != null && PLAY_LISTS != null

    fun PersistMusicPlayerActivityStatus(playerAction: PlayerAction, AlsoUpdateActivityIntent: Boolean = true) {
        if (CanPersistMusicPlayerActivityStatus()) {
            val ActivityStatusModel = MusicPlayerActivityStatus(
                relatedMusicListData!!,
                lyricListData!!,
                CurrentMusicPlayInfo!!,
                CurrentMusicContentInfo!!,
                CurrentMusicTotalDuration,
                MusicPlayer_Seekbar.progress,
                MusicPlayer_Seekbar.secondaryProgress,
                IS_ENABLE_LOOPING,
                CurrentPlayItemIndex,
                PLAY_LISTS!!
            )
            if (AlsoUpdateActivityIntent)
                PlayerService?.UpdateWakeupMusicPlayerActivityIntent(ActivityStatusModel)
            LAST_MUSIC_PLAYER_ACTIVITY_STATUS = ActivityStatusModel
            LAST_PLAYER_STATUS = playerAction
        }
    }


    companion object {
        @JvmStatic
        private var LAST_PLAYER_STATUS: PlayerAction? = null

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


    private fun SubscribeMusicPlayerStatus() {
        player?.looping(IS_ENABLE_LOOPING)
        player?.registerStateChangedListener(object : DefaultMusicPlayerStateChangedCallback() {
            override fun onLoadFailed(player: MusicPlayer) {
                ResetTimeIndicator()
            }

            override fun onPlaying(player: MusicPlayer) {
                MusicPlayer_TotalTime.text = Duration2Time(player.duration().toInt())
                PersistMusicPlayerActivityStatus(PlayerAction.PLAYING)
                beginQueryTimeStamp()
                MusicPlayer_Control_Play.setImageResource(R.drawable.ic_pause_red_600_24dp)
            }

            override fun onPausing(player: MusicPlayer) {
                PersistMusicPlayerActivityStatus(PlayerAction.PAUSED)
                stopQueryTimeStamp()
                MusicPlayer_Control_Play.setImageResource(R.drawable.ic_play_arrow_red_600_24dp)
            }

            override fun onStopping(player: MusicPlayer) {
                stopQueryTimeStamp()
                ResetTimeIndicator()
            }
            override fun onBuffering(player: MusicPlayer) {
                MusicPlayer_Seekbar.secondaryProgress = player.buffered()
            }
        })
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

    private fun StartService(NoNeedToStart: Boolean = false) {
        val intent = Intent(this, MusicPlayerService::class.java)
        if (!NoNeedToStart) {
            startService(intent)
        }
        bindService(intent, PlayerServiceConnection, Context.BIND_AUTO_CREATE)
        SubscribeMusicPlayerStatus()
    }

    override fun onDestroy() {
        if (player?.state() == PlayerAction.PLAYING || player?.state() == PlayerAction.PAUSED) {
            Log.d("MusicControlActivity", "应该保存当前Activity的状态信息.")
            PersistMusicPlayerActivityStatus(player!!.state())
        }
        unbindService(PlayerServiceConnection)
        super.onDestroy()
    }
}