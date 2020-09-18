package com.nemesiss.dev.piaprobox.Service.Player.NewPlayer.impl

import com.nemesiss.dev.piaprobox.Service.Player.NewPlayer.MusicPlayer
import com.nemesiss.dev.piaprobox.Service.Player.NewPlayer.MusicPlayerExtension
import com.nemesiss.dev.piaprobox.Service.Player.NewPlayer.PlayerAttribute
import com.nemesiss.dev.piaprobox.Service.Player.NewPlayer.PlayerStatus

class SimpleMusicPlayerImpl : MusicPlayer {
    override fun play(): PlayerStatus {
        TODO("Not yet implemented")
    }

    override fun play(timestamp: Long): PlayerStatus {
        TODO("Not yet implemented")
    }

    override fun pause(): PlayerStatus {
        TODO("Not yet implemented")
    }

    override fun stop(): PlayerStatus {
        TODO("Not yet implemented")
    }

    override fun currentTimestamp(): Long {
        TODO("Not yet implemented")
    }

    override fun attribute(): PlayerAttribute {
        TODO("Not yet implemented")
    }

    override fun registerExtension(extension: MusicPlayerExtension) {
        TODO("Not yet implemented")
    }

    override fun unregisterExtension(extension: MusicPlayerExtension) {
        TODO("Not yet implemented")
    }


}