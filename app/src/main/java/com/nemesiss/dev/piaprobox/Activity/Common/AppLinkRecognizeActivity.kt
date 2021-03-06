package com.nemesiss.dev.piaprobox.Activity.Common

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.nemesiss.dev.contentparser.model.RecommendItemModel
import com.nemesiss.dev.contentparser.model.RecommendItemModelImage
import com.nemesiss.dev.contentparser.model.RecommendItemModelText
import com.nemesiss.dev.piaprobox.Activity.Image.IllustratorImageProviderActivity
import com.nemesiss.dev.piaprobox.Activity.Image.IllustratorViewActivity2
import com.nemesiss.dev.piaprobox.Activity.Music.MusicControlActivity
import com.nemesiss.dev.piaprobox.Activity.Music.MusicPlayerActivity
import com.nemesiss.dev.piaprobox.Activity.Text.TextDetailActivity
import com.nemesiss.dev.piaprobox.model.resources.Constants
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.DaggerFetchFactory
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.handle
import kotlinx.android.synthetic.main.activity_app_link_recognize.*
import org.jsoup.Jsoup
import org.slf4j.getLogger

class AppLinkRecognizeActivity : PiaproboxBaseActivity() {

    companion object {
        private const val ACTION = "android.intent.action.VIEW"
    }

    private val mHandle = Handler(Looper.getMainLooper())
    private val log = getLogger<AppLinkRecognizeActivity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_link_recognize)
        val appLinkUri = intent?.data
        if (intent?.action == ACTION && appLinkUri != null) {
            matchAppLink(appLinkUri)
        } else {
            handleInvalidDataAndExit()
        }
    }

    private fun matchAppLink(uri: Uri) {
        showMatchingUri(uri)
        when (uri.scheme) {
            "https" -> {
                matchWebsiteLink(uri)
            }
            "piaprobox" -> {
                matchPiaproboxShareLink(uri)
            }
            else -> handleBrokenLink(uri)
        }
    }

    /**
     * If it's a normal piapro item website link such as: https://piapro.jp/t/AGfI
     * We cannot determine item type until we open page and parse html document.
     *
     * Music item: <div class="music-box-whole"/>
     * Illustration item: <div class="illust-whole"/>
     * Text item: <div id="_txt" />
     */
    private fun matchWebsiteLink(uri: Uri) {
        val uriString = uri.toString()
        DaggerFetchFactory
            .create()
            .fetcher()
            .visit(uriString)
            .goAsync(
                {
                    it.handle<String>(
                        { body ->
                            val html = Jsoup.parse(body)
                            val isMusic = html.getElementsByClass("music-box-whole").size > 0
                            val isIllustration = html.getElementsByClass("illust-whole").size > 0
                            val isText = html.getElementById("_txt") != null
                            when {
                                isMusic -> handleMusic(uriString)
                                isIllustration -> handleIllustration(uriString)
                                isText -> handleText(uriString)
                                else -> handleBrokenLink(uri)
                            }
                        },
                        { code, res ->
                            log.error(
                                "Error occurred when fetching item type info. Code: {}, responseBody: {}, response: {}",
                                code,
                                res.body?.string(),
                                res
                            )
                            handleMatchError("Error occurred when fetching item type info. Code: $code, response: ${res.body?.string()}")
                        })
                },
                { ex ->
                    // TODO handle error
                    log.error("Exception: ", ex)
                    handleMatchError("Error occurred due to unexpected exception: $ex. Please refer to log or issuing it.")
                })
    }


    /**
     * If it's a piaprobox in-app share link, we can feel free to match item type using the second fragment
     * like: piaprobox://share/music/AGfI
     *
     * music
     */
    private fun matchPiaproboxShareLink(uri: Uri) {
        val segments = uri.pathSegments
        if (segments.size < 2) {
            // lack of parts, match failed.
            handleBrokenLink(uri)
            return
        }
        val type = segments[0]
        val hash = segments[1]
        val websiteLink = Constants.Share.getWebsiteLink(hash)
        when (type) {
            Constants.Share.MUSIC -> handleMusic(websiteLink)
            Constants.Share.ILLUSTRATION -> handleIllustration(websiteLink)
            Constants.Share.TEXT -> handleText(websiteLink)
            else -> handleMatchError("Invalid item type: ${type}, original link: $uri")
        }
    }

    private fun handleMusic(websiteLink: String) {
        val intent = Intent(this, MusicControlActivity::class.java)
        intent.putExtra(MusicPlayerActivity.MUSIC_CONTENT_URL, websiteLink)
        intent.putExtra(MusicPlayerActivity.CLICK_ITEM_INDEX, 0)
        MusicPlayerActivity.PLAY_LISTS = listOf(RecommendItemModel().apply { URL = websiteLink })
        startActivity(intent)
        finish()
    }

    private fun handleIllustration(websiteLink: String) {
        val intent = Intent(this, IllustratorViewActivity2::class.java)
        intent.putExtra(IllustratorViewActivity2.CLICKED_ITEM_INDEX, 0)
        IllustratorImageProviderActivity.SetItemList(listOf(RecommendItemModelImage().apply { URL = websiteLink }))
        startActivity(intent)
        finish()
    }

    private fun handleText(websiteLink: String) {
        val intent = Intent(this, TextDetailActivity::class.java)
        intent.putExtra(TextDetailActivity.SHOWN_TEXT_INTENT_KEY, RecommendItemModelText().apply { URL = websiteLink })
        startActivity(intent)
        finish()
    }

    private fun showMatchingUri(uri: Uri) {
        applink_recognize_link.text = uri.toString()
    }

    // match error handlers.

    private fun handleInvalidDataAndExit() {
        log.error("Invalid data found!")
        runOnUiThread {
            applink_recognize_hint.text =
                "Cannot determinate app link. Invalid action or data.\nThis activity will exit in 3 seconds."
            closeInSeconds(3)
        }
    }

    private fun handleMatchError(err: String) {
        log.error("Match error: {}", err)
        runOnUiThread {
            applink_recognize_hint.text =
                "Cannot determinate app link due to error.\nThis activity will exit in 3 seconds."
            applink_recognize_link.text = err
            closeInSeconds(3)
        }
    }

    private fun handleBrokenLink(uri: Uri) {
        log.warn("Broken link: {}", uri)
        handleInvalidDataAndExit()
        runOnUiThread { applink_recognize_link.text = uri.toString() }
    }

    private fun closeInSeconds(sec: Int) {
        log.warn("Request to exit this activity in {} sec(s).", sec)
        mHandle.postDelayed({ finish() }, 1000L * sec)
    }
}
