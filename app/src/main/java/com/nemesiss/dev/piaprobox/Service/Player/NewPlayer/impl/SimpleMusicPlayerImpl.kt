package com.nemesiss.dev.piaprobox.Service.Player.NewPlayer.impl

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.AssetFileDescriptor
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.Player.NewPlayer.*
import java.io.FileInputStream
import java.lang.ref.SoftReference

/**
 *` 简单的音乐播放器，用于封装安卓原生MediaSource。
 */
open class SimpleMusicPlayerImpl(
    private val context: Context,
    private var isLooping: Boolean = false
) : MusicPlayer {

    private var player: MediaPlayer = MediaPlayer()

    private var isDestroyed: Boolean = false

    private var listeners: LinkedHashSet<MusicPlayerStateChangedListener> = LinkedHashSet(3)

    private var playingMediaReference: SoftReference<Any>? = null

    private var audioManager: AudioManager

    private var prepareFailedHandler = Handler {
        handlePlayerNextState(PlayerAction.STOPPED) {
            listeners.forEach { one -> one.onLoadFailed(this) }
        }
        true
    }

    private fun countDownForPrepareFailed() {
        prepareFailedHandler.sendEmptyMessageDelayed(101, 10 * 1000)
    }

    private fun indicatePrepareFinished() {
        prepareFailedHandler.removeMessages(101)
    }

    var currentAction = PlayerAction.STOPPED
        private set

    private var isPrepared = false

    var bufferedPercent = 0
        private set


    private var pendingPlay = false
    private val headphoneConnectIntentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    private val headPhoneConnectReceiver = HeadPhoneConnectReceiver()

    private inner class HeadPhoneConnectReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                Log.d("SimpleMusicPlayerImpl", "Headset unplugged! Pause!")
                // Pause the playback
                handlePlayerNextState(PlayerAction.PAUSED) {
                    listeners.forEach { one -> one.onPausing(this@SimpleMusicPlayerImpl) }
                }
            }
        }
    }

    private var audioFocusChangedListener: AudioManager.OnAudioFocusChangeListener

    init {
        player.setOnPreparedListener {
            isPrepared = true
            indicatePrepareFinished()
            handlePlayerNextState(PlayerAction.PLAYING) {
                listeners.forEach { one -> one.onPlaying(this) }
            }
        }
        player.setOnBufferingUpdateListener { _, percent ->
            bufferedPercent = percent
            listeners.forEach { one -> one.onBuffering(this) }
        }
        player.setOnCompletionListener {
            handlePlayerNextState(PlayerAction.STOPPED) {
                listeners.forEach { one ->
                    one.onStopping(this)
                    one.onPlayFinished(this)
                }
            }
        }

        context.registerReceiver(headPhoneConnectReceiver, headphoneConnectIntentFilter)

        audioFocusChangedListener = AudioManager.OnAudioFocusChangeListener { grantCode ->
            when (grantCode) {
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {
                    Log.d("SimpleMusicPlayerImpl", "AudioFocus is granted!")
                    if (pendingPlay) {
                        player.start()
                        pendingPlay = false
                    }
                }
                AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> Toast.makeText(
                    context,
                    context.getString(R.string.AudioFocusDelayHint),
                    Toast.LENGTH_SHORT
                ).show()
                AudioManager.AUDIOFOCUS_REQUEST_FAILED -> {
                    pendingPlay = false
                    Toast.makeText(
                        context,
                        context.getString(R.string.AudioFocusFailedHint),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        audioManager = context.getSystemService(AudioManager::class.java)
    }

    private fun requestPlay() {
        pendingPlay = true
        val responseCode = audioManager.requestAudioFocus(
            audioFocusChangedListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
        audioFocusChangedListener.onAudioFocusChange(responseCode)
    }

    private fun setCurrentPlayingMediaReference(source: Any) {
        playingMediaReference?.clear()
        playingMediaReference = SoftReference(source)
    }

    private fun detectPlayerNextStateByApplyingNewPlaying(source: Any): PlayerAction {
        if (playingMediaReference == null) {
            setCurrentPlayingMediaReference(source)
            return PlayerAction.PREPARING
        }
        val nextAction = if (true == playingMediaReference?.get()?.equals(source)) {
            when (currentAction) {
                PlayerAction.PLAYING -> PlayerAction.NO_ACTION
                PlayerAction.PREPARING -> PlayerAction.NO_ACTION
                PlayerAction.STOPPED -> PlayerAction.PLAYING
                PlayerAction.PAUSED -> PlayerAction.PLAYING
                else -> PlayerAction.PREPARING
            }
        } else {
            PlayerAction.PREPARING
        }
        setCurrentPlayingMediaReference(source)
        return nextAction
    }

    private fun resetFlags() {
        isPrepared = false
        bufferedPercent = 0
    }

    private fun prepareSource(source: Any) {
        when (source) {
            is AssetFileDescriptor -> doPreparing(source)
            is Uri -> doPreparing(source)
            is FileInputStream -> doPreparing(source)
        }
        countDownForPrepareFailed()
    }

    private fun doPreparing(source: AssetFileDescriptor) {
        resetFlags()
        resetPlayerState()
        player.setDataSource(source.fileDescriptor)
        player.prepareAsync()
    }

    private fun doPreparing(source: Uri) {
        resetFlags()
        resetPlayerState()
        player.setDataSource(context, source)
        player.prepareAsync()
    }

    private fun doPreparing(source: FileInputStream) {
        resetFlags()
        resetPlayerState()
        player.setDataSource(source.fd)
        player.prepareAsync()
    }

    private fun handlePlayerNextState(nextAction: PlayerAction, callbackIfStateChanged: () -> Unit = {}) {
        if (nextAction == PlayerAction.NO_ACTION) {
            return
        }
        when (currentAction) {
            PlayerAction.PREPARING -> when (nextAction) {
                PlayerAction.PREPARING -> {
                }
                PlayerAction.PLAYING -> {
                    if (isPrepared) requestPlay()
                }
                PlayerAction.PAUSED -> {
                    throw IllegalStateException("Cannot pause a preparing player.")
                }
                PlayerAction.STOPPED -> {
                    resetFlags()
                    resetPlayerState()
                }
                PlayerAction.NO_ACTION -> {
                }
            }
            PlayerAction.PLAYING -> when (nextAction) {
                PlayerAction.PREPARING -> {
                    resetFlags()
                    resetPlayerState()
                    playingMediaReference?.get()?.let { source ->
                        prepareSource(source)
                    }
                }
                PlayerAction.PLAYING -> {

                }
                PlayerAction.PAUSED -> {
                    player.pause()
                }
                PlayerAction.STOPPED -> {
                    player.stop()
                }
                PlayerAction.NO_ACTION -> {

                }
            }
            PlayerAction.PAUSED -> when (nextAction) {
                PlayerAction.PREPARING -> {
                    resetFlags()
                    resetPlayerState()
                    playingMediaReference?.get()?.let { source ->
                        prepareSource(source)
                    }
                }
                PlayerAction.PLAYING -> {
                    requestPlay()
                }
                PlayerAction.PAUSED -> {
                }
                PlayerAction.STOPPED -> {
                    player.stop()
                }
                PlayerAction.NO_ACTION -> {
                }
            }
            PlayerAction.STOPPED -> when (nextAction) {
                PlayerAction.PREPARING -> {
                    resetFlags()
                    resetPlayerState()
                    playingMediaReference?.get()?.let { source ->
                        prepareSource(source)
                    }
                }
                PlayerAction.PLAYING -> {
                    requestPlay()
                }
                PlayerAction.PAUSED -> {
                    throw IllegalStateException("Cannot pause a stopped player.")
                }
                PlayerAction.STOPPED -> {
                }
                PlayerAction.NO_ACTION -> {
                }
            }
            PlayerAction.NO_ACTION -> {
            }
        }
        val shouldExecuteCallback = currentAction != nextAction
        currentAction = nextAction
        if (shouldExecuteCallback) {
            try {
                callbackIfStateChanged()
            } catch (thr: Throwable) {
                Log.e("SimpleMusicPlayerImpl", "error occurred!", thr)
            }
        }
    }

    override fun play(source: AssetFileDescriptor): PlayerAction {
        checkStatus()
        handlePlayerNextState(
            detectPlayerNextStateByApplyingNewPlaying(source)
        ) {
            listeners.forEach { one -> one.onLoading(this) }
        }
        return currentAction
    }

    override fun play(source: Uri): PlayerAction {
        checkStatus()
        handlePlayerNextState(
            detectPlayerNextStateByApplyingNewPlaying(source)
        ) {
            listeners.forEach { one -> one.onLoading(this) }
        }
        return currentAction
    }

    override fun play(source: FileInputStream): PlayerAction {
        checkStatus()
        handlePlayerNextState(
            detectPlayerNextStateByApplyingNewPlaying(source)
        ) {
            listeners.forEach { one -> one.onLoading(this) }
        }
        return currentAction
    }

    override fun resume(): PlayerAction {
        handlePlayerNextState(PlayerAction.PLAYING) {
            listeners.forEach { one -> one.onPlaying(this) }
        }
        return currentAction
    }

    override fun pause(): PlayerAction {
        handlePlayerNextState(PlayerAction.PAUSED) {
            listeners.forEach { one -> one.onPausing(this) }
        }
        return currentAction
    }

    override fun stop(): PlayerAction {
        handlePlayerNextState(PlayerAction.STOPPED) {
            listeners.forEach { one -> one.onStopping(this) }
        }
        return currentAction
    }

    override fun seekTo(timestamp: Long) {
        if (currentAction == PlayerAction.PLAYING || currentAction == PlayerAction.PAUSED) {
            player.seekTo(timestamp.toInt())
            listeners.forEach { one -> one.onSeekTo(this) }
        } else {
            throw IllegalStateException("Cannot seek media because player is not in playing or pausing.")
        }
    }

    override fun seekTo(percent: Int) {
        if (currentAction == PlayerAction.PLAYING || currentAction == PlayerAction.PAUSED) {
            seekTo((player.duration * percent.toFloat() / 100).toLong())
        } else {
            throw IllegalStateException("Cannot seek media because player is not in playing or pausing.")
        }
    }

    override fun currentTimestamp(): Long {
        return player.currentPosition.toLong()
    }

    override fun duration(): Long {
        return player.duration.toLong()
    }

    override fun registerStateChangedListener(listener: MusicPlayerStateChangedListener): Boolean {
        listener.onRegistered(this)
        return listeners.add(listener)
    }

    override fun unregisterStateChangedListener(listener: MusicPlayerStateChangedListener): Boolean {
        listener.onUnregistered(this)
        return listeners.remove(listener)
    }

    override fun looping(): Boolean = isLooping

    override fun looping(loop: Boolean) {
        isLooping = loop
        player.isLooping = loop
    }

    override fun destroy() {
        if (isDestroyed) {
            tellAlreadyDestroyed()
        }
        try {
            player.stop()
        } catch (thr: Throwable) {
        }
        player.release()
        context.unregisterReceiver(headPhoneConnectReceiver)
        listeners.forEach { one -> one.onUnregistered(this) }
        listeners.clear()
    }

    override fun isDestroyed(): Boolean = isDestroyed

    override fun state(): PlayerAction = currentAction
    override fun buffered(): Int = bufferedPercent

    private fun checkStatus() {
        if (isDestroyed) {
            tellAlreadyDestroyed()
        }
    }

    private fun tellAlreadyDestroyed() {
        throw IllegalStateException("This MediaPlayer had been destroyed!")
    }

    private fun resetPlayerState() {
        try {
            player.stop()
        } catch (thr: Throwable) {
        }
        player.reset()
    }
}