package com.nemesiss.dev.piaprobox.Activity.Music

import android.os.Bundle
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
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.HTMLParser
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.DaggerFetchFactory
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.SimpleResponseHandler
import kotlinx.android.synthetic.main.music_player_layout.*
import org.jsoup.Jsoup

class MusicPlayerActivity : PiaproboxBaseActivity() {

    private lateinit var htmlParser: HTMLParser

    private var relatedMusicListData : List<RelatedMusicInfo>? = null
    private var relatedMusicListAdapter : RelatedMusicListAdapter? = null
    private var relatedMusicListLayoutManager : LinearLayoutManager? = null


    private var lyricListData : List<String>? = null
    private var lyricListAdapter : MusicLyricAdapter? = null
    private var lyricListLayoutManager : LinearLayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.music_player_layout)

        setSupportActionBar(MusicPlayer_Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        htmlParser = HTMLParser(this)
        val MusicContentUrl = intent.getStringExtra(MUSIC_CONTENT_URL)
        LoadMusicContentInfo(MusicContentUrl)
    }

    private fun LoadMusicContentInfo(Url : String) {
        if(Url.isEmpty()) {
            Toast.makeText(this, resources.getString(R.string.MusicContentUrlEmpty),Toast.LENGTH_SHORT).show()
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

    private fun LoadMusicPlayInfo(content : MusicContentInfo) {
        val Url = "https://piapro.jp/html5_player_popup/?id=${content.ContentID}&cdate=${content.CreateDate}&p=${content.Priority}"
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

    private fun ParseMusicContentInfo(HTMLString : String) {
        val root = Jsoup.parse(HTMLString)
        val parseMusicContentStep = htmlParser
                                        .Rules
                                        .getJSONObject("MusicContent")
                                        .getJSONArray("Steps")
        val contentInfo = htmlParser
                            .Parser
                            .GoSteps(root, parseMusicContentStep) as MusicContentInfo

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
        }
        ParseLyrics(contentInfo.Lyrics)
        LoadMusicPlayInfo(contentInfo)
    }

    private fun ParseMusicPlayInfo(HTMLString: String) {
        val root = Jsoup.parse(HTMLString)
        val steps = htmlParser.Rules.getJSONObject("MusicPlayInfo").getJSONArray("Steps")
        val playInfo = htmlParser.Parser.GoSteps(root,steps) as MusicPlayInfo
        val finalThumbUrl : String = GetAlbumThumb(playInfo.Thumb)
        runOnUiThread {
            Log.d("LoadMusicInfo","${finalThumbUrl}  ${playInfo.URL}")
            Glide.with(this)
                .load(finalThumbUrl)
                .priority(Priority.HIGH)
                .into(MusicPlayer_ThumbBackground)
        }
//        runOnUiThread {
//            MusicPlayer_ThumbUrl.text = playInfo.Thumb
//            MusicPlayer_MusicUrl.text = playInfo.URL
//        }
        HideLoadingIndicator(MusicPlayer_ContentContainer)
    }

    private fun ParseLyrics(LyricStr : String) {
        if(LyricStr.isEmpty()) {

        }
    }

    private fun LoadRelatedMusicsToView(data : List<RelatedMusicInfo>) {
        relatedMusicListData = data
        if(relatedMusicListAdapter == null) {
            relatedMusicListAdapter = RelatedMusicListAdapter(relatedMusicListData!!)
            MusicPlayer_RelatedMusic_RecyclerView.adapter = relatedMusicListAdapter
        }
        if(relatedMusicListLayoutManager == null) {
            relatedMusicListLayoutManager = LinearLayoutManager(this, LinearLayout.HORIZONTAL,false)
            MusicPlayer_RelatedMusic_RecyclerView.layoutManager = relatedMusicListLayoutManager
        }
        else {
            relatedMusicListAdapter?.items = relatedMusicListData!!
            relatedMusicListAdapter?.notifyDataSetChanged()
        }
    }



    companion object {
        @JvmStatic
        val MUSIC_CONTENT_URL = "MUSIC_CONTENT_URL"

        @JvmStatic
        fun GetAlbumThumb(ValueFromParser : String) : String {
            return if(ValueFromParser.contains("cdn")) {
                "http://${ValueFromParser}"
            } else {
                RecommendFragment.DefaultTagUrl + ValueFromParser
            }
        }
    }
}