package com.nemesiss.dev.piaprobox.Activity.Music

import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.nemesiss.dev.HTMLContentParser.Model.MusicContentInfo
import com.nemesiss.dev.HTMLContentParser.Model.MusicPlayInfo
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModel
import com.nemesiss.dev.HTMLContentParser.Model.RelatedMusicInfo
import com.nemesiss.dev.piaprobox.Activity.Common.PiaproboxBaseActivity
import com.nemesiss.dev.piaprobox.Adapter.MusicPlayer.MusicLyricAdapter
import com.nemesiss.dev.piaprobox.Adapter.MusicPlayer.RelatedMusicListAdapter
import com.nemesiss.dev.piaprobox.Model.CheckPermissionModel
import com.nemesiss.dev.piaprobox.Model.MusicPlayerActivityStatus
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.DaggerFactory.DaggerDownloadServiceFactory
import com.nemesiss.dev.piaprobox.Service.DaggerModules.DownloadServiceModules
import com.nemesiss.dev.piaprobox.Service.Download.DownloadService
import com.nemesiss.dev.piaprobox.Service.GlideApp
import com.nemesiss.dev.piaprobox.Service.HTMLParser
import com.nemesiss.dev.piaprobox.Service.HTMLParser.Companion.GetAlbumThumb
import com.nemesiss.dev.piaprobox.Service.Player.MusicPlayerService
import com.nemesiss.dev.piaprobox.Service.Player.NewPlayer.PlayerAction
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.DaggerFetchFactory
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.handle
import kotlinx.android.synthetic.main.music_player_layout.*
import org.jsoup.Jsoup
import javax.inject.Inject

open class MusicPlayerActivity : PiaproboxBaseActivity() {

    @Inject
    lateinit var downloadService: DownloadService

    private lateinit var htmlParser: HTMLParser
    protected var relatedMusicListData: List<RelatedMusicInfo>? = null
    private var relatedMusicListAdapter: RelatedMusicListAdapter? = null
    private var relatedMusicListLayoutManager: LinearLayoutManager? = null

    protected var lyricListData: List<String>? = null
    private var lyricListAdapter: MusicLyricAdapter? = null
    private var lyricListLayoutManager: LinearLayoutManager? = null

    protected var CurrentPlayItemIndex: Int = -1

    protected var CurrentMusicPlayInfo: MusicPlayInfo? = null
    protected var CurrentMusicContentInfo: MusicContentInfo? = null


    private val RECOVER_STATUS_RESOURCE_IS_OK: Boolean
        get() = LAST_MUSIC_PLAYER_ACTIVITY_STATUS != null

    private val MUSIC_ALBUM_LOAD_LISTENER = object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            return true
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            LAST_MUSIC_BITMAP = resource
            return false
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        HandleSwitchMusicIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.music_player_layout)

        DaggerDownloadServiceFactory
            .builder()
            .downloadServiceModules(DownloadServiceModules(this))
            .build()
            .inject(this)

        ShowToolbarBackIcon(MusicPlayer_Toolbar)
        // 关闭RecyclerView的嵌套滚动
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
            CurrentPlayItemIndex = intent.getIntExtra(CLICK_ITEM_INDEX, -1)
            val ClickToolbarIcon = intent.getBooleanExtra(CLICK_TOOLBAR_ICON, false)

            val Cond1 = (ClickToolbarIcon || MusicContentUrl == LAST_LOAD_CONTENT_URL)
            val Cond2 = RECOVER_STATUS_RESOURCE_IS_OK

            if (Cond1 && Cond2) {
                // 点击的和上次是一样的，恢复.
                RecoverActivityStatusFromPersistObject(LAST_MUSIC_PLAYER_ACTIVITY_STATUS!!)
            } else {
                LAST_MUSIC_PLAYER_ACTIVITY_STATUS = null
                LoadMusicContentInfo(MusicContentUrl, true)
            }
        }
        HandleSwitchMusicIntent(intent)
    }

    private fun HandleSwitchMusicIntent(intent: Intent?) {
        if (intent?.action == "NEXT") {
            Log.d("MusicPlayer", "感知到下一曲Intent")
            NextMusic()
        } else if (intent?.action == "PREV") {
            Log.d("MusicPlayer", "感知到上一曲Intent")
            PrevMusic()
        }
    }

    private fun RecoverActivityStatusFromPersistObject(activityStatus: MusicPlayerActivityStatus) {
        Log.d("MusicPlayerActivity", "MusicPlayerActivity  开始恢复上一次的Activity状态")
        relatedMusicListData = activityStatus.relatedMusicListData
        lyricListData = activityStatus.lyrics
        CurrentMusicPlayInfo = activityStatus.currentPlayMusicInfo
        CurrentMusicContentInfo = activityStatus.currentPlayMusicContentInfo
        MusicPlayer_Toolbar.title = activityStatus.currentPlayMusicContentInfo.Title
        CurrentPlayItemIndex = activityStatus.currentPlayItemIndex
        PLAY_LISTS = activityStatus.playLists

        if (LAST_MUSIC_BITMAP != null) {
            GlideApp.with(this)
                .load(LAST_MUSIC_BITMAP)
                .priority(Priority.HIGH)
                .into(MusicPlayer_ThumbBackground)
        } else {
            GlideLoadThumbToImageView(CurrentMusicPlayInfo?.Thumb ?: "")
        }
        ActivateLyricRecyclerViewAdapter()
        ActivateRelatedMusicRecyclerViewAdapter()
    }

    private fun LoadMusicContentInfo(Url: String, ShouldUpdateRelatedMusicList: Boolean) {
        if (Url.isEmpty()) {
            Toast.makeText(this, resources.getString(R.string.MusicContentUrlEmpty), Toast.LENGTH_SHORT).show()
            return
        }

        if (PLAY_LISTS != null && CurrentPlayItemIndex != -1) {
            val item = PLAY_LISTS!![CurrentPlayItemIndex]
            // 直接在这里更新.
            MusicPlayer_Toolbar.title = item.ItemName
        }
//        重置播放变量:
//        CurrentMusicPlayInfo = null

        LAST_LOAD_CONTENT_URL = Url
        ShowLoadingIndicator(MusicPlayer_ContentContainer)
        DaggerFetchFactory.create()
            .fetcher()
            .visit(Url)
            .goAsync({ response ->
                response.handle<String>({
                    ParseMusicContentInfo(it, ShouldUpdateRelatedMusicList)
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

    private fun ParseMusicContentInfo(HTMLString: String, ShouldUpdateRelatedMusicList: Boolean) {
        val root = Jsoup.parse(HTMLString)
        val parseMusicContentStep = htmlParser
            .Rules
            .getJSONObject("MusicContent")
            .getJSONArray("Steps")
        val contentInfo = htmlParser
            .Parser
            .GoSteps(root, parseMusicContentStep) as MusicContentInfo

        CurrentMusicContentInfo = contentInfo

        if (ShouldUpdateRelatedMusicList) {
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
                response.handle<String>({
                    ParseMusicPlayInfo(it)
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

    private fun GlideLoadThumbToImageView(url: String) {
        try {
            GlideApp.with(this)
                .load(url)
                .priority(Priority.HIGH)
                .addListener(MUSIC_ALBUM_LOAD_LISTENER)
                .into(MusicPlayer_ThumbBackground)
        } catch (e: Exception) {
        }
    }

    private fun ParseMusicPlayInfo(HTMLString: String) {
        val root = Jsoup.parse(HTMLString)
        val steps = htmlParser.Rules.getJSONObject("MusicPlayInfo").getJSONArray("Steps")
        val playInfo = htmlParser.Parser.GoSteps(root, steps) as MusicPlayInfo
        val finalThumbUrl: String = GetAlbumThumb(playInfo.Thumb)
        runOnUiThread {
            Log.d("LoadMusicInfo", finalThumbUrl)
            GlideLoadThumbToImageView(finalThumbUrl)
        }
        CurrentMusicPlayInfo = playInfo

        val intent = Intent(this, MusicPlayerService::class.java)
        intent.action = "UPDATE_INFO"
        intent.putExtra("UpdateMusicContentInfo", CurrentMusicContentInfo)
        intent.putExtra("WillPlayMusicURL", playInfo.URL)

        (this as? MusicControlActivity)?.PersistMusicPlayerActivityStatus(PlayerAction.STOPPED, true)

        startService(intent)
        HideLoadingIndicator(MusicPlayer_ContentContainer)
    }

    private fun ParseLyrics(LyricStr: String) {
        lyricListData = if (LyricStr.isEmpty()) {
            MusicLyricAdapter.BuildNoLyricList()
        } else {
            LyricStr.split(" ")
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
        PLAY_LISTS = relatedMusicListData!!.map { related ->
            RecommendItemModel().apply {
                ArtistName = related.Artist
                Thumb = related.Thumb
                ItemName = related.Title
                URL = related.URL
            }
        }
        CurrentPlayItemIndex = index
        LoadMusicContentInfo(item.URL, false)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.player_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        super.onOptionsItemSelected(item)
        when (item?.itemId) {
            R.id.MusicPlayer_Toolbar_Download -> {
                if (CurrentMusicPlayInfo == null || CurrentMusicContentInfo == null || CurrentMusicContentInfo!!.Title.isEmpty()) {
                    Toast.makeText(this, R.string.MusicPlayInfoIsntPrepared, Toast.LENGTH_SHORT).show()
                } else {
                    ConfirmDownloadRequest()
                }
            }
        }
        return true
    }

    private fun ConfirmDownloadRequest() {
        AlertDialog.Builder(this)
            .setTitle(R.string.DownloadMusicRequestDialogTitle)
            .setMessage(R.string.DownloadMusicRequestDialogMessgae)
            .setPositiveButton("OK") { _, _ ->
                downloadService.DownloadMusic(
                    "${CurrentMusicContentInfo?.Title ?: System.currentTimeMillis()}.mp3",
                    CurrentMusicPlayInfo?.URL!!,
                    CheckPermissionModel(this)
                )
            }
            .setNegativeButton("Cancel") { _, _ -> }
            .show()
    }

    protected fun NextMusic() {
        if (PLAY_LISTS != null && CurrentPlayItemIndex != -1 && CurrentPlayItemIndex + 1 < PLAY_LISTS!!.size) {
            val nextItem = PLAY_LISTS!![CurrentPlayItemIndex + 1]
            MusicPlayer_Toolbar.title = nextItem.ItemName
            LoadMusicContentInfo(HTMLParser.WrapDomain(nextItem.URL), false)
            CurrentPlayItemIndex++
        }
    }

    protected fun PrevMusic() {
        if (PLAY_LISTS != null && CurrentPlayItemIndex != -1 && CurrentPlayItemIndex - 1 >= 0) {
            val nextItem = PLAY_LISTS!![CurrentPlayItemIndex - 1]
            MusicPlayer_Toolbar.title = nextItem.ItemName
            LoadMusicContentInfo(HTMLParser.WrapDomain(nextItem.URL), false)
            CurrentPlayItemIndex--
        }
    }

    companion object {
        @JvmStatic
        val MUSIC_CONTENT_URL = "MUSIC_CONTENT_URL"

        @JvmStatic
        val CLICK_TOOLBAR_ICON = "CLICK_TOOLBAR_ICON"

        @JvmStatic
        val CLICK_ITEM_INDEX = "CLICKED_ITEM_INDEX"

        @JvmStatic
        val PERSIST_STATUS_INTENT_KEY = "ActivityLastStatus"

        @JvmStatic
        var LAST_MUSIC_PLAYER_ACTIVITY_STATUS: MusicPlayerActivityStatus? = null

        @JvmStatic
        private var LAST_MUSIC_BITMAP: Drawable? = null

        @JvmStatic
        var LAST_LOAD_CONTENT_URL = ""

        @JvmStatic
        var PLAY_LISTS: List<RecommendItemModel>? = null

        @JvmStatic
        fun CleanStaticResources() {
            LAST_MUSIC_PLAYER_ACTIVITY_STATUS = null
            LAST_MUSIC_BITMAP = null
            PLAY_LISTS = null
        }
    }
}