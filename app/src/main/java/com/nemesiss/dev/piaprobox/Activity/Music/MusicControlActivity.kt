package com.nemesiss.dev.piaprobox.Activity.Music

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
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


    companion object {
        @JvmStatic
        private var LAST_PLAYER_STATUS: PlayerAction? = null

        @JvmStatic
        private fun Duration2Time(duration: Int): String {
            val min = duration / 1000 / 60
            val sec = duration / 1000 % 60
            return (if (min < 10) "0$min" else min.toString() + "") + ":" + if (sec < 10) "0$sec" else sec.toString() + ""
        }

        @JvmStatic
        private fun Progress2Duration(progress: Int, totalDuration: Int): Int {
            return (progress.toFloat() / 100 * totalDuration).toInt()
        }
    }


    private var fromNotificationIntent = false
    private var newMusicLoaded = false
    private var isSeeking = false
    private var isEnableLooping = false
        @Synchronized
        set(value) {
            player?.looping(value)
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

    private val PlayerServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(componentName: ComponentName?) {
            Log.d("MusicControlActivity", "音乐播放器服务取消绑定.")
            PlayerService = null
        }

        override fun onServiceConnected(componentName: ComponentName?, service: IBinder?) {
            PlayerService = (service as MusicPlayerService.MusicPlayerBinder).getService()
            Log.d("MusicControlActivity", "音乐播放器服务Bind完成  ${PlayerService.hashCode()}")
            PlayerService?.let { ps -> player = ps.player }
            SubscribeMusicPlayerStatus()
        }
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
                        PlayerAction.PLAYING -> { player?.pause() }
                        PlayerAction.PAUSED -> { player?.resume() }
                        PlayerAction.STOPPED -> { player?.resume() }
                        else -> {}
                    }
                } else {
                    PendingPrepareURL = CurrentMusicPlayInfo?.URL ?: ""
                    StartMusicPlayService(true)
                }
            }, {
                val intent = Intent(this, MusicDetailActivity::class.java)
                intent.putExtra(MusicDetailActivity.MUSIC_CONTENT_INFO_INTENT_KEY, CurrentMusicContentInfo)
                startActivity(intent)
            }, {
                isEnableLooping = !isEnableLooping
            }, {
                NextMusic()
            }, {
                PrevMusic()
            })
        InitSeekbarController()
    }

    private fun LoadUserPreferenceSetup() {
        isEnableLooping = Persistence.GetMusicPlayerLoopStatus()
    }

    private fun PrepareActivityStatus() {
        val status = intent.getSerializableExtra(PERSIST_STATUS_INTENT_KEY)
        StartMusicPlayService(onlyBindService = status != null)
        if (status != null) {
            // 从通知栏消息过来.
            fromNotificationIntent = true
            newMusicLoaded = false
            val activityStatus = status as MusicPlayerActivityStatus
            RecoverActivityStatusFromPersistObject(activityStatus)
        } else {
            fromNotificationIntent = false
            // 从点击RecommendItem过来
            val MusicContentUrl = intent.getStringExtra(MUSIC_CONTENT_URL) ?: ""
            if (LAST_MUSIC_PLAYER_ACTIVITY_STATUS != null) {
                // 恢复
                RecoverActivityStatusFromPersistObject(LAST_MUSIC_PLAYER_ACTIVITY_STATUS!!)
            } else if (MusicContentUrl.isNotEmpty()) {
                newMusicLoaded = true
            }
        }
    }

    private fun handleQueryTimeStamp(message: Message): Boolean {
        MusicPlayer_CurrentTime.text = Duration2Time(player?.currentTimestamp()?.toInt() ?: 0)
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

    private fun InitSeekbarController() {
        MusicPlayer_Seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    MusicPlayer_CurrentTime.text =
                            (Duration2Time(Progress2Duration(progress, CurrentMusicTotalDuration)))
                }
            }

            override fun onStartTrackingTouch(seekbar: SeekBar?) {
                isSeeking = true
            }

            override fun onStopTrackingTouch(seekbar: SeekBar?) {
                seekbar?.apply {
                    isSeeking = false
                    player?.seekTo(progress)
                }
            }
        })
    }

    private fun RecoverActivityStatusFromPersistObject(activityStatus: MusicPlayerActivityStatus) {
        Log.d("MusicControlActivity", "MusicControlActivity  开始恢复上一次的Activity状态")
        CurrentMusicTotalDuration = activityStatus.musicTotalDuration
        MusicPlayer_Seekbar.progress = activityStatus.seekbarProgress
        MusicPlayer_CurrentTime.text = Duration2Time(
            Progress2Duration(activityStatus.seekbarProgress, activityStatus.musicTotalDuration)
        )
        MusicPlayer_TotalTime.text = Duration2Time(CurrentMusicTotalDuration)
        MusicPlayer_Seekbar.secondaryProgress = activityStatus.bufferBarProgress
    }

    private fun CanPersistMusicPlayerActivityStatus() =
            relatedMusicListData != null &&
                    lyricListData != null &&
                    CurrentMusicContentInfo != null &&
                    CurrentMusicPlayInfo != null && PLAY_LISTS != null

    fun PersistMusicPlayerActivityStatus(playerAction: PlayerAction, AlsoUpdateActivityIntent: Boolean) {
        if (CanPersistMusicPlayerActivityStatus()) {
            val ActivityStatusModel = MusicPlayerActivityStatus(
                    relatedMusicListData!!,
                    lyricListData!!,
                    CurrentMusicPlayInfo!!,
                    CurrentMusicContentInfo!!,
                    CurrentMusicTotalDuration,
                    MusicPlayer_Seekbar.progress,
                    MusicPlayer_Seekbar.secondaryProgress,
                    isEnableLooping,
                    CurrentPlayItemIndex,
                    PLAY_LISTS!!
            )
            if (AlsoUpdateActivityIntent)
                PlayerService?.UpdateWakeupMusicPlayerActivityIntent(ActivityStatusModel)
            LAST_MUSIC_PLAYER_ACTIVITY_STATUS = ActivityStatusModel
            LAST_PLAYER_STATUS = playerAction
        }
    }




    private fun SubscribeMusicPlayerStatus() {
        player?.looping(isEnableLooping)
        player?.registerStateChangedListener(object : DefaultMusicPlayerStateChangedCallback() {

            override fun onRegistered(player: MusicPlayer) {
                // 尝试必要的状态恢复。
                recoverState(player)
            }

            override fun onLoading(player: MusicPlayer) {
                MusicPlayer_Control_Play.setImageResource(R.drawable.ic_more_horiz_red_600_24dp)
            }

            override fun onPlaying(player: MusicPlayer) {
                MusicPlayer_Seekbar.isEnabled = true
                CurrentMusicTotalDuration = player.duration().toInt()
                player.looping(isEnableLooping)
                MusicPlayer_TotalTime.text = Duration2Time(CurrentMusicTotalDuration)
                PersistMusicPlayerActivityStatus(PlayerAction.PLAYING, true)
                beginQueryTimeStamp()
                MusicPlayer_Control_Play.setImageResource(R.drawable.ic_pause_red_600_24dp)
            }

            override fun onPausing(player: MusicPlayer) {
                PersistMusicPlayerActivityStatus(PlayerAction.PAUSED, true)
                stopQueryTimeStamp()
                MusicPlayer_Control_Play.setImageResource(R.drawable.ic_play_arrow_red_600_24dp)
            }

            override fun onLoadFailed(player: MusicPlayer) {
                stopQueryTimeStamp()
                ResetTimeIndicator()
                MusicPlayer_Control_Play.setImageResource(R.drawable.ic_play_arrow_red_600_24dp)
            }

            override fun onStopping(player: MusicPlayer) {
                stopQueryTimeStamp()
                repeat(10) { ResetTimeIndicator() }
                MusicPlayer_Control_Play.setImageResource(R.drawable.ic_play_arrow_red_600_24dp)
            }

            override fun onPlayFinished(player: MusicPlayer) {
                MusicPlayer_Seekbar.isEnabled = false
                MusicPlayer_Control_Play.setImageResource(R.drawable.ic_play_arrow_red_600_24dp)
            }

            override fun onBuffering(player: MusicPlayer) {
                MusicPlayer_Seekbar.secondaryProgress = player.buffered()
            }

            override fun onUnregistered(player: MusicPlayer) {
                stopQueryTimeStamp()
                ResetTimeIndicator()
                MusicPlayer_Control_Play.setImageResource(R.drawable.ic_play_arrow_red_600_24dp)
            }

            private fun recoverState(player: MusicPlayer) {
                when (player.state()) {
                    PlayerAction.PLAYING -> {
                        PendingPrepareURL = ""
                        beginQueryTimeStamp()
                        MusicPlayer_Control_Play.setImageResource(R.drawable.ic_pause_red_600_24dp)
                    }
                    PlayerAction.PAUSED -> {
                        MusicPlayer_Control_Play.setImageResource(R.drawable.ic_play_arrow_red_600_24dp)
                    }
                    PlayerAction.STOPPED -> {
                        if (PendingPrepareURL.isNotEmpty()) {
                            player.play(Uri.parse(PendingPrepareURL))
                        }
                        MusicPlayer_Control_Play.setImageResource(R.drawable.ic_play_arrow_red_600_24dp)
                    }
                    else -> {}
                }
            }
        })
    }

    private fun ResetTimeIndicator() {
        CurrentMusicTotalDuration = 0
        MusicPlayer_Seekbar.progress = 0
        MusicPlayer_Seekbar.secondaryProgress = 0
        MusicPlayer_CurrentTime.text = Duration2Time(0)
        MusicPlayer_TotalTime.text = Duration2Time(0)
    }

    private fun StartMusicPlayService(onlyBindService: Boolean = false) {
        val intent = Intent(this, MusicPlayerService::class.java)
        if (!onlyBindService) {
            startService(intent)
        }
        bindService(intent, PlayerServiceConnection, Context.BIND_AUTO_CREATE)
        SubscribeMusicPlayerStatus()
    }

    override fun onDestroy() {
        if (player?.state() == PlayerAction.PLAYING || player?.state() == PlayerAction.PAUSED) {
            PersistMusicPlayerActivityStatus(player!!.state(), true)
        }
        unbindService(PlayerServiceConnection)
        super.onDestroy()
    }
}