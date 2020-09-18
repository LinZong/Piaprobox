package com.nemesiss.dev.piaprobox.Service.Player.NewPlayer

interface MusicPlayer {

    fun play(): PlayerStatus

    fun play(timestamp: Long): PlayerStatus

    fun pause(): PlayerStatus

    fun stop(): PlayerStatus

    fun currentTimestamp(): Long

    fun attribute(): PlayerAttribute

    fun attribute(attribute: PlayerAttribute)

    fun registerExtension(extension: MusicPlayerExtension)

    fun unregisterExtension(extension: MusicPlayerExtension)
}