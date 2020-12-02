package com.nemesiss.dev.piaprobox.Service.Player.NewPlayer

interface MusicPlayerStateChangedListener {

    fun onRegistered(player: MusicPlayer)

    fun onUnregistered(player: MusicPlayer)

    fun onLoading(player: MusicPlayer)

    fun onLoadFailed(player: MusicPlayer)

    fun onPlaying(player: MusicPlayer)

    fun onPlayFinished(player: MusicPlayer)

    fun onPausing(player: MusicPlayer)

    fun onStopping(player: MusicPlayer)

    fun onSeekTo(player: MusicPlayer)

    fun onBuffering(player: MusicPlayer)
}