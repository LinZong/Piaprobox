package com.nemesiss.dev.piaprobox.Activity.Music

import android.os.Bundle
import android.widget.Toast
import com.nemesiss.dev.HTMLContentParser.Model.MusicContentInfo
import com.nemesiss.dev.piaprobox.Activity.Common.PiaproboxBaseActivity
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.HTMLParser
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.DaggerFetchFactory
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.SimpleResponseHandler
import kotlinx.android.synthetic.main.music_player_layout.*
import org.jsoup.Jsoup

class MusicPlayerActivity : PiaproboxBaseActivity() {

    private lateinit var htmlParser: HTMLParser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.music_player_layout)

        setSupportActionBar(MusicPlayer_Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        htmlParser = HTMLParser(this)
        val MusicContentUrl = intent.getStringExtra(MUSIC_CONTENT_URL)
        if(MusicContentUrl.isEmpty()) {
            Toast.makeText(this, resources.getString(R.string.MusicContentUrlEmpty),Toast.LENGTH_SHORT).show()
        }
        LoadMusicInfo(MusicContentUrl)
    }

    private fun LoadMusicInfo(Url : String) {
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

    private fun ParseMusicContentInfo(HTMLString : String) {
        val root = Jsoup.parse(HTMLString)
        val rule = htmlParser.Rules.getJSONObject("MusicContent")
        val steps = rule.getJSONArray("Steps")
        val contentInfo = htmlParser.Parser.GoSteps(root, steps) as MusicContentInfo

        runOnUiThread {
            MusicPlayer_Toolbar.title = contentInfo.Title
            MusicPlayer_MusicUrl.text = contentInfo.ContentID
            MusicPlayer_ThumbUrl.text = contentInfo.CreateDate
        }
        HideLoadingIndicator(MusicPlayer_ContentContainer)
    }
    companion object {
        @JvmStatic
        val MUSIC_CONTENT_URL = "MUSIC_CONTENT_URL"
    }
}