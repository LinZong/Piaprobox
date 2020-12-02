package com.nemesiss.dev.piaprobox.Service.Player.NewPlayer

import android.content.res.AssetFileDescriptor
import android.net.Uri
import java.io.FileInputStream

interface MusicPlayer {

    fun play(source: AssetFileDescriptor): PlayerAction

    fun play(source: Uri): PlayerAction

    fun play(source: FileInputStream): PlayerAction

    fun resume(): PlayerAction

    fun pause(): PlayerAction

    fun stop(): PlayerAction

    fun seekTo(timestamp: Long)

    fun seekTo(percent: Int)

    fun currentTimestamp(): Long

    fun duration(): Long

    fun registerStateChangedListener(listener: MusicPlayerStateChangedListener): Boolean

    fun unregisterStateChangedListener(listener: MusicPlayerStateChangedListener): Boolean

    fun looping(): Boolean

    fun looping(loop: Boolean)

    fun destroy()

    fun isDestroyed(): Boolean

    fun state(): PlayerAction

    fun buffered(): Int
}