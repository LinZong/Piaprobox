package com.nemesiss.dev.piaprobox.Activity.Music

import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.nemesiss.dev.contentparser.model.MusicContentInfo
import com.nemesiss.dev.contentparser.model.MusicPlayInfo
import com.nemesiss.dev.contentparser.model.RecommendItemModel
import com.nemesiss.dev.contentparser.model.RelatedMusicInfo
import com.nemesiss.dev.piaprobox.Activity.Common.PiaproboxBaseActivity
import com.nemesiss.dev.piaprobox.Adapter.MusicPlayer.MusicLyricAdapter
import com.nemesiss.dev.piaprobox.Adapter.MusicPlayer.RelatedMusicListAdapter
import com.nemesiss.dev.piaprobox.model.CheckPermissionModel
import com.nemesiss.dev.piaprobox.model.MusicPlayerActivityStatus
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.AsyncExecutor
import com.nemesiss.dev.piaprobox.Service.DaggerFactory.DaggerDownloadServiceFactory
import com.nemesiss.dev.piaprobox.Service.DaggerFactory.DaggerImageCacheFactory
import com.nemesiss.dev.piaprobox.Service.DaggerModules.DownloadServiceModules
import com.nemesiss.dev.piaprobox.Service.DaggerModules.ImageCacheModules
import com.nemesiss.dev.piaprobox.Service.Download.DownloadService
import com.nemesiss.dev.piaprobox.Service.GlideApp
import com.nemesiss.dev.piaprobox.Service.HTMLParser
import com.nemesiss.dev.piaprobox.Service.HTMLParser.Companion.GetAlbumThumb
import com.nemesiss.dev.piaprobox.Service.ImageCache
import com.nemesiss.dev.piaprobox.Service.Player.MusicPlayerService
import com.nemesiss.dev.piaprobox.Service.Player.NewPlayer.PlayerAction
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.DaggerFetchFactory
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.handle
import kotlinx.android.synthetic.main.music_player_layout.*
import org.jsoup.Jsoup
import org.slf4j.getLogger
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.inject.Inject


private class MusicInfoHolder(val contentUrl: String, private val htmlParser: HTMLParser) {


    private val fetcher = DaggerFetchFactory.create().fetcher()


    lateinit var contentInfo: MusicContentInfo
        private set

    lateinit var playInfo: MusicPlayInfo
        private set

    var relatedMusics = emptyList<RelatedMusicInfo>()
        private set

    private var cdl = CountDownLatch(2)

    private var error: Throwable? = null

    @Volatile
    private var loading = false

    private val step get() = (2 - cdl.count).toInt()

    val isPrepared get() = step == 2

    private companion object {
        private val log = getLogger<MusicInfoHolder>()
    }

    constructor(
        contentUrl: String,
        htmlParser: HTMLParser,
        contentInfo: MusicContentInfo,
        playInfo: MusicPlayInfo
    ) : this(contentUrl, htmlParser) {
        this.contentInfo = contentInfo
        this.playInfo = playInfo
        // let cdl finished.
        repeat(2) { cdl.countDown() }
    }

    fun await() {
        emitFetchIfNeeded()
        cdl.await()
        throwPendingErrorIfExists()
    }

    fun await(timeout: Long, unit: TimeUnit): Boolean {
        emitFetchIfNeeded()
        val done = cdl.await(timeout, unit)
        throwPendingErrorIfExists()
        return done
    }

    fun fetch() = emitFetchIfNeeded()

    private fun emitFetchIfNeeded() {
        if (!loading && step == 0) {
             // clean error, go ahead.
            error = null
            next()
        }
    }

    private fun next() {
        try {
            when (step) {
                0 -> {
                    loading = true; loadContentInfo()
                }
                1 -> loadPlayInfo()
                2 -> {
                    loading = false
                }
            }
        } catch (e: Throwable) {
            log.error("Load $contentUrl failed", e)
            error = e
            resetStatus()
        }
    }

    private fun resetStatus() {
        loading = false
        repeat(2) { cdl.countDown() }
        cdl = CountDownLatch(2)
    }

    private fun throwPendingErrorIfExists() {
        val err = error
        if (err != null) {
            error = null
            throw err
        }
    }

    private fun loadContentInfo() {
        log.info("Loading content info for url: $contentUrl ...")

        val response = fetcher.visit(contentUrl).go()
        response.handle<String>(
            { body -> parseContentInfo(body) },
            { httpCode, error -> throw Exception("Invalid response error, HttpCode: $httpCode, Response: ${error.body?.string()}") })
    }

    private fun loadPlayInfo() {
        val url =
            "https://piapro.jp/html5_player_popup/?id=${contentInfo.ContentID}&cdate=${contentInfo.CreateDate}&p=${contentInfo.Priority}"
        log.info("Loading play info for url: $url")
        val response = fetcher.visit(url).go()
        response.handle<String>(
            { body -> parsePlayInfo(body) },
            { httpCode, error -> throw Exception("Invalid response error, HttpCode: $httpCode, Response: ${error.body?.string()}") })
    }

    private fun parseContentInfo(body: String) {
        val root = Jsoup.parse(body)

        val parseMusicContentStep = htmlParser
            .Rules
            .getJSONObject("MusicContent")
            .getJSONArray("Steps")
        this.contentInfo = htmlParser
            .Parser
            .GoSteps(root, parseMusicContentStep) as MusicContentInfo

        val parseRelatedMusicInfoSteps = htmlParser
            .Rules
            .getJSONObject("RelatedMusic")
            .getJSONArray("Steps")


        // 推荐列表拿不到无所谓。
        try {
            this.relatedMusics = (htmlParser
                .Parser
                .GoSteps(root, parseRelatedMusicInfoSteps) as Array<*>)
                .map { it as RelatedMusicInfo }
        } catch (t: Throwable) {
            log.error("Cannot get related musics for url: $contentUrl", t)
        }
        cdl.countDown()
        next()
    }

    private fun parsePlayInfo(body: String) {
        val root = Jsoup.parse(body)
        val steps = htmlParser.Rules.getJSONObject("MusicPlayInfo").getJSONArray("Steps")
        val playInfo = htmlParser.Parser.GoSteps(root, steps) as MusicPlayInfo
        playInfo.Thumb = GetAlbumThumb(playInfo.Thumb)
        this.playInfo = playInfo
        log.warn("Url: $contentUrl finish loading successfully!")

        cdl.countDown()
        next()
    }
}


open class MusicPlayerActivity : PiaproboxBaseActivity() {

    // 专门用来缓存Cache的Async，异步任务很多，单独开线程
    private val cacheFetcherAsync = AsyncExecutor(20, 40)

    private val log = getLogger<MusicPlayerActivity>()

    @Inject
    lateinit var downloadService: DownloadService

    @Inject
    lateinit var imageCache: ImageCache

    private lateinit var htmlParser: HTMLParser
    protected var relatedMusicListData: List<RelatedMusicInfo>? = null
    private var relatedMusicListAdapter: RelatedMusicListAdapter? = null
    private var relatedMusicListLayoutManager: LinearLayoutManager? = null

    protected var lyricListData: List<String>? = null
    private var lyricListAdapter: MusicLyricAdapter? = null
    private var lyricListLayoutManager: LinearLayoutManager? = null

    protected var currentPlayItemIndex: Int = -1

    protected var currentMusicPlayInfo: MusicPlayInfo? = null
    protected var currentMusicContentInfo: MusicContentInfo? = null


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
            if (resource != null) {
                val thumbUrl = currentMusicPlayInfo?.Thumb ?: return false
                val fileName = Uri.parse(thumbUrl).lastPathSegment ?: return false
                imageCache.cache(resource, fileName)
                log.info("Image $fileName cached!")
            }
            return false
        }
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleSwitchMusicIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.music_player_layout)

        DaggerDownloadServiceFactory
            .builder()
            .downloadServiceModules(DownloadServiceModules(this))
            .build()
            .inject(this)

        DaggerImageCacheFactory
            .builder()
            .imageCacheModules(ImageCacheModules(this))
            .build()
            .inject(this)

        showToolbarBackIcon(MusicPlayer_Toolbar)
        // 关闭RecyclerView的嵌套滚动
        ViewCompat.setNestedScrollingEnabled(MusicPlayer_Lyric_RecyclerView, false)
        MusicPlayer_Lyric_RecyclerView.isNestedScrollingEnabled = false

        htmlParser = HTMLParser(this)
        // 从通知栏消息过来
        val status = intent.getSerializableExtra(PERSIST_STATUS_INTENT_KEY)
        if (status != null) {
            val activityStatus = status as MusicPlayerActivityStatus
            recoverActivityStatusFromPersistObject(activityStatus)
        } else {
            // 从RecommendItem点击过来
            val musicContentUrl = intent.getStringExtra(MUSIC_CONTENT_URL) ?: ""
            currentPlayItemIndex = intent.getIntExtra(CLICK_ITEM_INDEX, -1)
            val clickToolbarIcon = intent.getBooleanExtra(CLICK_TOOLBAR_ICON, false)

            val cond1 = (clickToolbarIcon || musicContentUrl == LAST_LOAD_CONTENT_URL)
            val cond2 = RECOVER_STATUS_RESOURCE_IS_OK

            if (cond1 && cond2) {
                // 点击的和上次是一样的，恢复.
                recoverActivityStatusFromPersistObject(LAST_MUSIC_PLAYER_ACTIVITY_STATUS!!)
            } else {
                LAST_MUSIC_PLAYER_ACTIVITY_STATUS = null
//                LoadMusicContentInfo(MusicContentUrl, true)
                loadMusicContentInfo(currentPlayItemIndex, true)
            }
        }
        handleSwitchMusicIntent(intent)
    }

    private fun handleSwitchMusicIntent(intent: Intent?) {
        when (intent?.action) {
            "NEXT" -> nextMusic()
            "PREV" -> prevMusic()
        }
    }

    private fun recoverActivityStatusFromPersistObject(activityStatus: MusicPlayerActivityStatus) {
        relatedMusicListData = activityStatus.relatedMusicListData
        lyricListData = activityStatus.lyrics
        currentMusicPlayInfo = activityStatus.currentPlayMusicInfo
        currentMusicContentInfo = activityStatus.currentPlayMusicContentInfo
        MusicPlayer_Toolbar.title = activityStatus.currentPlayMusicContentInfo.Title
        currentPlayItemIndex = activityStatus.currentPlayItemIndex
        PLAY_LISTS = activityStatus.playLists

        val thumbUrl = currentMusicPlayInfo?.Thumb
        if (thumbUrl != null) {
            loadThumbToBackgroundImage(thumbUrl)
        }

        activateLyricRecyclerViewAdapter()
        activateRelatedMusicRecyclerViewAdapter()
    }

    @Synchronized
    private fun buildPlayListCacheIfNeeded(force: Boolean = false) {
        val playList = PLAY_LISTS ?: return
        if (playList.isEmpty()) return
        if (!force) {
            if (playListCache.isNotEmpty()) {
                // check if cache is outdated.
                val firstUrl = HTMLParser.wrapDomain(playList[0].URL)
                val firstCacheUrl = playListCache[0].contentUrl
                if (firstUrl == firstCacheUrl) return
            }
        }
        log.warn("Rebuilding playlist cache...")
        playListCache.clear()
        playListCache +=
            playList
                .map { v -> HTMLParser.wrapDomain(v.URL) }
                .map { url -> MusicInfoHolder(url, htmlParser) }

        fetchPlayListCacheInfoAsync()
        log.warn("Finish rebuild playlist cache!!!")
    }

    private fun fetchPlayListCacheInfoAsync() {
        for (item in playListCache) {
            cacheFetcherAsync.SendTask { item.fetch() }
        }
    }

    private fun loadMusicContentInfo(playListIndex: Int, loadRelatedMusics: Boolean = false) {
        buildPlayListCacheIfNeeded()

        val playListItemHolder = playListCache[playListIndex]

        val async = AsyncExecutor.INSTANCE
        async.SendTask awaitTask@{
            try {
                if (!playListItemHolder.isPrepared) {
                    // 只有这个MusicInfoHolder没有完全加载的时候，才显示LoadingIndicator，避免闪屏。
                    ShowLoadingIndicator(MusicPlayer_ContentContainer)
                    val done = playListItemHolder.await(15, TimeUnit.SECONDS)
                    if (!done) {
                        LoadFailedTips(-11, "Load music content failed, timeout after 15 seconds.")
                        return@awaitTask
                    }
                }
                // must be prepared.
                onNewMusicInfoLoaded(playListItemHolder, loadRelatedMusics)
            } catch (t: Throwable) {
                // 加载过程中出现异常
                LoadFailedTips(-10, t.message ?: "Unknown error.")
            } finally {
                HideLoadingIndicator(MusicPlayer_ContentContainer)
            }
        }
    }

    private fun onNewMusicInfoLoaded(holder: MusicInfoHolder, refreshRelatedMusics: Boolean = false) {
        val contentInfo = holder.contentInfo
        val playInfo = holder.playInfo

        LAST_LOAD_CONTENT_URL = holder.contentUrl
        currentMusicContentInfo = contentInfo
        currentMusicPlayInfo = playInfo

        runOnUiThread {
            MusicPlayer_Toolbar.title = contentInfo.Title
            parseLyrics(contentInfo.Lyrics)
            loadThumbToBackgroundImage(playInfo.Thumb)
            if (refreshRelatedMusics) {
                loadRelatedMusics(holder.relatedMusics)
            }
            playMusic(contentInfo, playInfo)
        }
    }

    private fun loadThumbToBackgroundImage(url: String) {

        // first, touch cache for previous result.
        val fileName = Uri.parse(url).lastPathSegment
        if (fileName != null) {
            val cacheImage = imageCache[fileName]
            log.warn("Image $fileName hit cache!")
            if (cacheImage != null) {
                GlideApp.with(this)
                    .load(cacheImage)
                    .priority(Priority.HIGH)
                    .into(MusicPlayer_ThumbBackground)
                log.warn("Image $fileName loaded from cache!")
                return
            }
        }
        log.warn("Image $fileName has no cache, load ahead.")
        try {
            GlideApp.with(this)
                .load(url)
                .priority(Priority.HIGH)
                .addListener(MUSIC_ALBUM_LOAD_LISTENER)
                .into(MusicPlayer_ThumbBackground)
        } catch (e: Exception) { }
    }

    private fun playMusic(contentInfo: MusicContentInfo, playInfo: MusicPlayInfo) {
        val intent = Intent(this, MusicPlayerService::class.java)
        intent.action = "UPDATE_INFO"
        intent.putExtra("UpdateMusicContentInfo", contentInfo)
        intent.putExtra("WillPlayMusicURL", playInfo.URL)
        (this as? MusicControlActivity)?.persistMusicPlayerActivityStatus(PlayerAction.STOPPED, true)
        startService(intent)
    }

    private fun parseLyrics(rawLyrics: String) {
        lyricListData = if (rawLyrics.isEmpty()) {
            MusicLyricAdapter.BuildNoLyricList()
        } else {
            rawLyrics.split(" ")
        }
        activateLyricRecyclerViewAdapter()
    }

    private fun activateLyricRecyclerViewAdapter() {
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

    private fun activateRelatedMusicRecyclerViewAdapter() {
        if (relatedMusicListAdapter == null) {
            relatedMusicListAdapter = RelatedMusicListAdapter(relatedMusicListData!!, this::onRelatedItemSelected)
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

    private fun loadRelatedMusics(data: List<RelatedMusicInfo>) {
        relatedMusicListData = data
        activateRelatedMusicRecyclerViewAdapter()
    }

    private fun onRelatedItemSelected(index: Int) {
        PLAY_LISTS = relatedMusicListData!!.map { related ->
            RecommendItemModel().apply {
                ArtistName = related.Artist
                Thumb = related.Thumb
                ItemName = related.Title
                URL = related.URL
            }
        }
        currentPlayItemIndex = index
        // force rebuild play list cache.
        buildPlayListCacheIfNeeded(true)
        loadMusicContentInfo(index, false)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.player_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        super.onOptionsItemSelected(item)
        when (item?.itemId) {
            R.id.MusicPlayer_Toolbar_Download -> {
                if (currentMusicPlayInfo == null || currentMusicContentInfo == null || currentMusicContentInfo!!.Title.isEmpty()) {
                    Toast.makeText(this, R.string.MusicPlayInfoIsntPrepared, Toast.LENGTH_SHORT).show()
                } else {
                    confirmDownloadRequest()
                }
            }
        }
        return true
    }

    private fun confirmDownloadRequest() {
        AlertDialog.Builder(this)
            .setTitle(R.string.DownloadMusicRequestDialogTitle)
            .setMessage(R.string.DownloadMusicRequestDialogMessgae)
            .setPositiveButton("OK") { _, _ ->
                downloadService.DownloadMusic(
                    "${currentMusicContentInfo?.Title ?: System.currentTimeMillis()}.mp3",
                    currentMusicPlayInfo?.URL!!,
                    CheckPermissionModel(this)
                )
            }
            .setNegativeButton("Cancel") { _, _ -> }
            .show()
    }

    protected fun nextMusic() {
        if (PLAY_LISTS != null && currentPlayItemIndex != -1 && currentPlayItemIndex + 1 < PLAY_LISTS!!.size) {
//            val nextItem = PLAY_LISTS!![CurrentPlayItemIndex + 1]
//            MusicPlayer_Toolbar.title = nextItem.ItemName
            loadMusicContentInfo(currentPlayItemIndex + 1, false)
            currentPlayItemIndex++
        }
    }

    protected fun prevMusic() {
        if (PLAY_LISTS != null && currentPlayItemIndex != -1 && currentPlayItemIndex - 1 >= 0) {
            loadMusicContentInfo(currentPlayItemIndex - 1, false)
            currentPlayItemIndex--
        }
    }

    override fun onDestroy() {
        cacheFetcherAsync.shutdownNow()
        super.onDestroy()
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
        var LAST_LOAD_CONTENT_URL = ""

        @JvmStatic
        var PLAY_LISTS: List<RecommendItemModel>? = null

        @JvmStatic
        private val playListCache: ArrayList<MusicInfoHolder> = arrayListOf()


        @JvmStatic
        fun cleanStaticResources() {
            LAST_MUSIC_PLAYER_ACTIVITY_STATUS = null
            // 音乐播放器没在播放，删掉当前播放列表
            if (!MusicPlayerService.IS_FOREGROUND) {
                PLAY_LISTS = null
                playListCache.clear()
            }
        }
    }
}