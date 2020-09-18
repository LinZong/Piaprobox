package com.nemesiss.dev.piaprobox.Service.Player.NewPlayer

interface MusicPlayerExtension {

    fun onRegistered(player: MusicPlayer)

    fun onUnregistered(player: MusicPlayer)

    fun onLoading(player: MusicPlayer)

    fun onPlaying(player: MusicPlayer)

    fun onPausing(player: MusicPlayer)

    fun onStopping(player: MusicPlayer)

    fun onTimeElapsing(player: MusicPlayer)
}