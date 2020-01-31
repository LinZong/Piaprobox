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

        setSupportActionBar(MusicPlayer_Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ViewCompat.setNestedScrollingEnabled(MusicPlayer_Lyric_RecyclerView, false)
        MusicPlayer_Lyric_RecyclerView.isNestedScrollingEnabled = false

        htmlParser = HTMLParser(this)

        val status = intent.getSerializableExtra(PERSIST_STATUS_INTENT_KEY)
        if (status != null) {
            val activityStatus = status as MusicPlayerActivityStatus
            relatedMusicListData = activityStatus.relatedMusicListData
            lyricListData = activityStatus.lyrics
            CurrentPlayMusicUrl = activityStatus.currentPlayMusicURL
            CurrentContentInfo = activityStatus.currentPlayMusicContentInfo
            MusicPlayer_Toolbar.title = activityStatus.currentPlayMusicContentInfo.Title
            ActivateLyricRecyclerViewAdapter()
            ActivateRelatedMusicRecyclerViewAdapter()
        } else {
            val MusicContentUrl = intent.getStringExtra(MUSIC_CONTENT_URL) ?: ""
            LoadMusicContentInfo(MusicContentUrl)
        }
    }


    private fun LoadMusicContentInfo(Url: String) {
        if (Url.isEmpty()) {
            Toast.makeText(this, resources.getString(R.string.MusicContentUrlEmpty), Toast.LENGTH_SHORT).show()
            return
        }
        ShowLoadingIndicator(MusicPlayer_ContentContainer)
        DaggerFetchFactory.create()
            .fetcher()
            .visit(Url)
            .goAsync({ response ->
                SimpleResponseHandler(response, String::class)
                    .Handle({
                        ParseMusicContentInfo(it as String)
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

    private fun ParseMusicContentInfo(HTMLString: String) {
        val root = Jsoup.parse(HTMLString)
        val parseMusicContentStep = htmlParser
            .Rules
            .getJSONObject("MusicContent")
            .getJSONArray("Steps")
        val contentInfo = htmlParser
            .Parser
            .GoSteps(root, parseMusicContentStep) as MusicContentInfo

        CurrentContentInfo = contentInfo

        val parseRelatedMusicInfoSteps = htmlParser
            .Rules
            .getJSONObject("RelatedMusic")
            .getJSONArray("Steps")
        val relatedMusics = (htmlParser
            .Parser
            .GoSteps(root, parseRelatedMusicInfoSteps) as Array<*>)
            .map { it as RelatedMusicInfo }
        runOnUiThread {
            MusicPlayer_Toolbar.title = contentInfo.Title
            LoadRelatedMusicsToView(relatedMusics)
            ParseLyrics(contentInfo.Lyrics)
        }
        LoadMusicPlayInfo(contentInfo)
    }

    private fun ParseMusicPlayInfo(HTMLString: String) {
        val root = Jsoup.parse(HTMLString)
        val steps = htmlParser.Rules.getJSONObject("MusicPlayInfo").getJSONArray("Steps")
        val playInfo = htmlParser.Parser.GoSteps(root, steps) as MusicPlayInfo
        val finalThumbUrl: String = GetAlbumThumb(playInfo.Thumb)
        runOnUiThread {
            Log.d("LoadMusicInfo", "${finalThumbUrl}  ${playInfo.URL}")
            Glide.with(this)
                .load(finalThumbUrl)
                .priority(Priority.HIGH)
                .into(MusicPlayer_ThumbBackground)
        }
        CurrentPlayMusicUrl = playInfo.URL
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
        LoadMusicContentInfo(item.URL)
        if(CurrentPlayMusicUrl.isNotEmpty()) {
            Log.d("MusicPlayerActivity", "命令停止")
            var intent = Intent(this, MusicPlayerService::class.java)
            intent.action = "STOP"
            startService(intent)
        }
    }

    companion object {
        @JvmStatic
        val MUSIC_CONTENT_URL = "MUSIC_CONTENT_URL"

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
    }
}