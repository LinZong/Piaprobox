package com.nemesiss.dev.piaprobox.Service.Player.NewPlayer.extensions

import com.nemesiss.dev.piaprobox.Service.Player.NewPlayer.MusicPlayer
import com.nemesiss.dev.piaprobox.Service.Player.NewPlayer.MusicPlayerExtension
import com.nemesiss.dev.piaprobox.Service.Player.NewPlayer.PlayerSource

class PlaylistExtension(
    var playImmediateWhenSwitchPlayingItem: Boolean = true,
    var playImmediateWhenApplyingNewPlaylist: Boolean = false,
    var playList: LinkedHashMap<PlayerSource, Any>
) : MusicPlayerExtension {

    var looping: Boolean = false

    override fun onRegistered(player: MusicPlayer) {
        TODO("Not yet implemented")
    }

    override fun onUnregistered(player: MusicPlayer) {
        TODO("Not yet implemented")
    }

    override fun onLoading(player: MusicPlayer) {
        TODO("Not yet implemented")
    }

    override fun onPlaying(player: MusicPlayer) {
        TODO("Not yet implemented")
    }

    override fun onPausing(player: MusicPlayer) {
        TODO("Not yet implemented")
    }

    override fun onStopping(player: MusicPlayer) {
        TODO("Not yet implemented")
    }

    override fun onTimeElapsing(player: MusicPlayer) {
        TODO("Not yet implemented")
    }

    fun next() {

    }

    fun prev() {

    }
}