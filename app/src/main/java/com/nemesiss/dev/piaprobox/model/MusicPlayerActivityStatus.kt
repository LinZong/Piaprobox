package com.nemesiss.dev.piaprobox.model

import com.nemesiss.dev.contentparser.model.MusicContentInfo
import com.nemesiss.dev.contentparser.model.MusicPlayInfo
import com.nemesiss.dev.contentparser.model.RecommendItemModel
import com.nemesiss.dev.contentparser.model.RelatedMusicInfo
import java.io.Serializable

data class MusicPlayerActivityStatus(
    val relatedMusicListData: List<RelatedMusicInfo>,
    val lyrics: List<String>,
    val currentPlayMusicInfo: MusicPlayInfo,
    val currentPlayMusicContentInfo: MusicContentInfo,
    val musicTotalDuration: Int,
    val seekbarProgress: Int,
    val bufferBarProgress: Int,
    val openSingleLooping: Boolean,
    val currentPlayItemIndex: Int,
    val playLists: List<RecommendItemModel>
) : Serializable