package com.nemesiss.dev.piaprobox.Activity.Music

import android.content.Intent
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.nemesiss.dev.HTMLContentParser.Model.MusicContentInfo
import com.nemesiss.dev.HTMLContentParser.Model.MusicPlayInfo
import com.nemesiss.dev.HTMLContentParser.Model.RelatedMusicInfo
import com.nemesiss.dev.piaprobox.Activity.Common.PiaproboxBaseActivity
import com.nemesiss.dev.piaprobox.Adapter.MusicPage.MusicLyricAdapter
import com.nemesiss.dev.piaprobox.Adapter.MusicPage.RelatedMusicListAdapter
import com.nemesiss.dev.piaprobox.Fragment.Main.RecommendFragment
import com.nemesiss.dev.piaprobox.Model.MusicPlayerActivityStatus
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.HTMLParser
import com.nemesiss.dev.piaprobox.Service.MusicPlayer.MusicPlayerService
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.DaggerFetchFactory
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.SimpleResponseHandler
import kotlinx.android.synthetic.main.music_player_layout.*
import org.jsoup.Jsoup

open class MusicPlayerActivity : PiaproboxBaseActivity() {

    private lateinit var htmlParser: HTMLParser
    protected var relatedMusicListData: List<RelatedMusicInfo>? = null
    private var relatedMusicListAdapter: RelatedMusicListAdapter? = null
    private var relatedMusicListLayoutManager: LinearLayoutManager? = null
    protected var lyricListData: List<String>? = null
    private var lyricListAdapter: MusicLyricAdapter? = null
    private var lyricListLayoutManager: LinearLayoutManager? = null
    protected var CurrentPlayMusicUrl: String = ""
    protected var CurrentContentInfo: MusicContentInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.music_player_layout)

        ShowToolbarBackIcon(MusicPlayer_Toolbar)

        ViewCompat.setNestedScrollingEnabled(MusicPlayer_Lyric_RecyclerView, false)
        MusicPlayer_Lyric_RecyclerView.isNestedScrollingEnabled = false

        htmlParser = HTMLParser(this)

        // 从通知栏消息过来

        val status = intent.getSerializableExtra(PERSIST_STATUS_INTENT_KEY)
        if (status != null) {
            val activityStatus = status as MusicPlayerActivityStatus
            RecoverActivityStatusFromPersistObject(activityStatus)
        } else {
            // 从RecommendItem点击过来
            val MusicContentUrl = intent.getStringExtra(MUSIC_CONTENT_URL) ?: ""
            val ClickToolbarIcon = intent.getBooleanExtra(CLICK_TOOLBAR_ICON, false)
            if((MusicContentUrl == LAST_LOAD_CONTENT_URL || ClickToolbarIcon) && LAST_MUSIC_BITMAP != null && LAST_MUSIC_PLAYER_ACTIVITY_STATUS != null) {
                // 点击的和上次是一样的，恢复.
                RecoverActivityStatusFromPersistObject(LAST_MUSIC_PLAYER_ACTIVITY_STATUS!!)
            }
            else {
                LAST_MUSIC_PLAYER_ACTIVITY_STATUS = null
                LoadMusicContentInfo(MusicContentUrl,true)
            }
        }
    }

    private fun RecoverActivityStatusFromPersistObject(activityStatus : MusicPlayerActivityStatus) {
        Log.d("MusicPlayerActivity", "MusicPlayerActivity  开始恢复上一次的Activity状态")
        relatedMusicListData = activityStatus.relatedMusicListData
        lyricListData = activityStatus.lyrics
        CurrentPlayMusicUrl = activityStatus.currentPlayMusicURL
        CurrentContentInfo = activityStatus.currentPlayMusicContentInfo
        MusicPlayer_Toolbar.title = activityStatus.currentPlayMusicContentInfo.Title
        MusicPlayer_ThumbBackground.setImageDrawable(LAST_MUSIC_BITMAP)

        ActivateLyricRecyclerViewAdapter()
        ActivateRelatedMusicRecyclerViewAdapter()
    }

    private fun LoadMusicContentInfo(Url: String, ShouldUpdateRelatedMusicList : Boolean) {
        if (Url.isEmpty()) {
            Toast.makeText(this, resources.getString(R.string.MusicContentUrlEmpty), Toast.LENGTH_SHORT).show()
            return
        }
        LAST_LOAD_CONTENT_URL = Url
        ShowLoadingIndicator(MusicPlayer_ContentContainer)
        DaggerFetchFactory.create()
            .fetcher()
            .visit(Url)
            .goAsync({ response ->
                SimpleResponseHandler(response, String::class)
                    .Handle({
                        ParseMusicContentInfo(it as String, ShouldUpdateRelatedMusicList)
                    }, { code, _ ->
                        HideLoadingIndicator(MusicPlayer_ContentContainer)
                        LoadFailedTips(code, resources.getString(R.string.Error_Page_Load_Failed))
                    })
            }, { e ->
                runOnUiThread {
                    HideLoadingIndicator(MusicPlayer_ContentContainer)
                    LoadFailedTips(-4, e.message ?: "")
                }
            })
    }

    private fun ParseMusicContentInfo(HTMLString: String,ShouldUpdateRelatedMusicList : Boolean) {
        val root = Jsoup.parse(HTMLString)
        val parseMusicContentStep = htmlParser
            .Rules
            .getJSONObject("MusicContent")
            .getJSONArray("Steps")
        val contentInfo = htmlParser
            .Parser
            .GoSteps(root, parseMusicContentStep) as MusicContentInfo

        CurrentContentInfo = contentInfo

        if(ShouldUpdateRelatedMusicList) {
            val parseRelatedMusicInfoSteps = htmlParser
                .Rules
                .getJSONObject("RelatedMusic")
                .getJSONArray("Steps")
            val relatedMusics = (htmlParser
                .Parser
                .GoSteps(root, parseRelatedMusicInfoSteps) as Array<*>)
                .map { it as RelatedMusicInfo }

            runOnUiThread {
                LoadRelatedMusicsToView(relatedMusics)
            }
        }

        Log.d("MusicPlayerActivity", "命令更新通知信息")
        runOnUiThread {
            MusicPlayer_Toolbar.title = contentInfo.Title
            ParseLyrics(contentInfo.Lyrics)
        }
        LoadMusicPlayInfo(contentInfo)
    }

    private fun LoadMusicPlayInfo(content: MusicContentInfo) {
        val Url =
            "https://piapro.jp/html5_player_popup/?id=${content.ContentID}&cdate=${content.CreateDate}&p=${content.Priority}"
        DaggerFetchFactory.create()
            .fetcher()
            .visit(Url)
            .goAsync({ response ->
                SimpleResponseHandler(response, String::class)
                    .Handle({
                        ParseMusicPlayInfo(it as String)
                    }, { code, _ ->
                        HideLoadingIndicator(MusicPlayer_ContentContainer)
                        LoadFailedTips(code, resources.getString(R.string.Error_Page_Load_Failed))
                    })
            }, { e ->
                runOnUiThread {
                    HideLoadingIndicator(MusicPlayer_ContentContainer)
                    LoadFailedTips(-4, e.message ?: "")
                }
            })
    }



    private fun ParseMusicPlayInfo(HTMLString: String) {
        val root = Jsoup.parse(HTMLString)
        val steps = htmlParser.Rules.getJSONObject("MusicPlayInfo").getJSONArray("Steps")
        val playInfo = htmlParser.Parser.GoSteps(root, steps) as MusicPlayInfo
        val finalThumbUrl: String = GetAlbumThumb(playInfo.Thumb)
        runOnUiThread {
//            Log.d("LoadMusicInfo", "$finalThumbUrl  ${playInfo.URL}")
            Glide.with(this)
                .load(finalThumbUrl)
                .priority(Priority.HIGH)
                .into(object : SimpleTarget<GlideDrawable>() {
                    override fun onResourceReady(
                        resource: GlideDrawable?,
                        glideAnimation: GlideAnimation<in GlideDrawable>?
                    ) {
                        LAST_MUSIC_BITMAP = resource
                        MusicPlayer_ThumbBackground.setImageDrawable(resource)
                    }
                })
        }
        CurrentPlayMusicUrl = playInfo.URL
        val intent = Intent(this, MusicPlayerService::class.java)
        intent.action = "UPDATE_INFO"
        intent.putExtra("UpdateMusicContentInfo",CurrentContentInfo)
        intent.putExtra("WillPlayMusicURL",playInfo.URL)
        startService(intent)

//        runOnUiThread {
//            MusicPlayer_ThumbUrl.text = playInfo.Thumb
//            MusicPlayer_MusicUrl.text = playInfo.URL
//        }
        HideLoadingIndicator(MusicPlayer_ContentContainer)
    }

    private fun ParseLyrics(LyricStr: String) {
        if (LyricStr.isEmpty()) {
            lyricListData = MusicLyricAdapter.BuildNoLyricList()
        } else {
            lyricListData = LyricStr.split(" ")
        }
        ActivateLyricRecyclerViewAdapter()
    }

    private fun ActivateLyricRecyclerViewAdapter() {
        if (lyricListAdapter == null) {
            lyricListAdapter = MusicLyricAdapter(lyricListData!!)
            MusicPlayer_Lyric_RecyclerView.adapter = lyricListAdapter
        }
        if (lyricListLayoutManager == null) {
            lyricListLayoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
            MusicPlayer_Lyric_RecyclerView.layoutManager = lyricListLayoutManager
        } else {
            lyricListAdapter?.items = lyricListData!!
            lyricListAdapter?.notifyDataSetChanged()
        }
        MusicPlayer_Lyric_RecyclerView_Container.post {
            val LyricViewHeight = MusicPlayer_Lyric_RecyclerView_Container.height
            MusicPlayer_Lyric_RecyclerView.setPadding(0, LyricViewHeight / 2, 0, LyricViewHeight / 2)
        }
    }

    private fun ActivateRelatedMusicRecyclerViewAdapter() {
        if (relatedMusicListAdapter == null) {
            relatedMusicListAdapter = RelatedMusicListAdapter(relatedMusicListData!!, this::RelatedItemSelected)
            MusicPlayer_RelatedMusic_RecyclerView.adapter = relatedMusicListAdapter
        }
        if (relatedMusicListLayoutManager == null) {
            relatedMusicListLayoutManager = LinearLayoutManager(this, LinearLayout.HORIZONTAL, false)
            MusicPlayer_RelatedMusic_RecyclerView.layoutManager = relatedMusicListLayoutManager
        } else {
            relatedMusicListAdapter?.items = relatedMusicListData!!
            relatedMusicListAdapter?.notifyDataSetChanged()
        }
        MusicPlayer_RelatedMusic_RecyclerView.scrollToPosition(0)
    }

    private fun LoadRelatedMusicsToView(data: List<RelatedMusicInfo>) {
        relatedMusicListData = data
        ActivateRelatedMusicRecyclerViewAdapter()
    }

    private fun RelatedItemSelected(index: Int) {
        val item = relatedMusicListData!![index]
        LoadMusicContentInfo(item.URL,false)
    }



    companion object {
        @JvmStatic
        val MUSIC_CONTENT_URL = "MUSIC_CONTENT_URL"

        @JvmStatic
        val CLICK_TOOLBAR_ICON = "CLICK_TOOLBAR_ICON"

        @JvmStatic
        fun GetAlbumThumb(ValueFromParser: String): String {
            return if (ValueFromParser.contains("cdn")) {
                "http://${ValueFromParser}"
            } else {
                RecommendFragment.DefaultTagUrl + ValueFromParser
            }
        }

        @JvmStatic
        val PERSIST_STATUS_INTENT_KEY = "ActivityLastStatus"

        @JvmStatic
        var LAST_MUSIC_PLAYER_ACTIVITY_STATUS : MusicPlayerActivityStatus? = null

        @JvmStatic
        private var LAST_MUSIC_BITMAP : GlideDrawable? = null

        @JvmStatic
        var LAST_LOAD_CONTENT_URL = ""
    }
}