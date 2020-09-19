package com.nemesiss.dev.piaprobox.Model

import com.nemesiss.dev.HTMLContentParser.Model.MusicContentInfo
import com.nemesiss.dev.HTMLContentParser.Model.MusicPlayInfo
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModel
import com.nemesiss.dev.HTMLContentParser.Model.RelatedMusicInfo
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