package com.nemesiss.dev.piaprobox.model

import com.nemesiss.dev.piaprobox.Service.Player.NewPlayer.PlayerAction

data class MusicNotificationModel(val SongName : String,
                                  val ArtistName : String,
                                  val CurrAction : PlayerAction)
