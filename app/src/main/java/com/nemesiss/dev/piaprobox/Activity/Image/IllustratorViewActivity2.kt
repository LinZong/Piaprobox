package com.nemesiss.dev.piaprobox.Activity.Image

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.nemesiss.dev.HTMLContentParser.Model.ImageContentInfo
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModelImage
import com.nemesiss.dev.HTMLContentParser.Model.RelatedImageInfo
import com.nemesiss.dev.piaprobox.Adapter.IllustratorViewer.IllustratorViewPageFragmentAdapter
import com.nemesiss.dev.piaprobox.Fragment.ImageViewer.IllustratorViewFragment
import com.nemesiss.dev.piaprobox.Model.CheckPermissionModel
import com.nemesiss.dev.piaprobox.Model.Image.IllustratorViewFragmentViewModel
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.AsyncExecutor
import com.nemesiss.dev.piaprobox.Service.DaggerFactory.DaggerAsyncExecutorFactory
import com.nemesiss.dev.piaprobox.Service.DaggerFactory.DaggerDownloadServiceFactory
import com.nemesiss.dev.piaprobox.Service.DaggerFactory.DaggerErrorHandlerFactory
import com.nemesiss.dev.piaprobox.Service.DaggerFactory.DaggerHTMParserFactory
import com.nemesiss.dev.piaprobox.Service.DaggerModules.DownloadServiceModules
import com.nemesiss.dev.piaprobox.Service.DaggerModules.ErrorHandlerModules
import com.nemesiss.dev.piaprobox.Service.DaggerModules.HTMLParserModules
import com.nemesiss.dev.piaprobox.Service.Download.DownloadService
import com.nemesiss.dev.piaprobox.Service.GlobalErrorHandler.ParseContentErrorHandler
import com.nemesiss.dev.piaprobox.Service.HTMLParser
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.DaggerFetchFactory
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.handle
import com.nemesiss.dev.piaprobox.Util.AppUtil
import com.nemesiss.dev.piaprobox.Util.BaseOnPageChangeListener
import kotlinx.android.synthetic.main.illustrator_view_activity2.*
import org.jsoup.Jsoup
import java.io.File
import java.lang.Exception
import javax.inject.Inject

class IllustratorViewActivity2 : IllustratorImageProviderActivity() {
    companion object {
        @JvmStatic
        val REENTER_RESULT_CODE = 6789

        @JvmStatic
        val CLICKED_ITEM_INDEX = "CLICKED_ITEM_INDEX"

        @JvmStatic
        val IMAGE_PRE_SHOWN_IMAGE_INTENT_KEY = "IMAGE_PRE_SHOWN_IMAGE_INTENT_KEY"
    }

    @Inject
    lateinit var htmlParser: HTMLParser

    @Inject
    lateinit var asyncExecutor: AsyncExecutor

    @Inject
    lateinit var downloader: DownloadService

    lateinit var errorHandler: ParseContentErrorHandler

    // 状态相关变量
    private var ItemPages = ArrayList<IllustratorViewFragment>()

    private var ItemPageViewModelCache = SparseArray<IllustratorViewFragmentViewModel>()

    private var LoadingItemPageViewModel = SparseArray<IllustratorViewFragmentViewModel>()

    private var CURRENT_SHOW_IMAGE_INDEX = 0

    override fun onDestroy() {
        Illustrator2_Item_Pager.removeAllViews()
        ItemPageViewModelCache.clear()
        LoadingItemPageViewModel.clear()
        ItemPages.clear()
        super.onDestroy()
    }

    private fun ShowLineLoadingIndicator() {
        Illustrator_LoadingIndicator.visibility = View.VISIBLE
    }

    private fun HideLineLoadingIndicator() {
        Illustrator_LoadingIndicator.visibility = View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.illustrator_view_activity2)

        // Dagger inject to activity.
        DaggerHTMParserFactory
            .builder()
            .hTMLParserModules(HTMLParserModules(this))
            .build()
            .inject(this)

        DaggerAsyncExecutorFactory.builder().hTMLParserModules(HTMLParserModules(this)).build().inject(this)

        DaggerDownloadServiceFactory
            .builder()
            .downloadServiceModules(DownloadServiceModules(this))
            .build()
            .inject(this)


        errorHandler =
            DaggerErrorHandlerFactory.builder().errorHandlerModules(ErrorHandlerModules(this)).build().handler()

        val ClickedIndex = intent.getIntExtra(CLICKED_ITEM_INDEX, 0)

        // 暂停共享元素动画播放
        supportPostponeEnterTransition()
        // 把Fragment加载进来
        InitFragmentPager(ClickedIndex)
    }

    private fun InitFragmentPager(FirstShowIndex: Int) {
        if (CAN_VIEW_ITEM_LIST != null) {
            IntRange(0, CAN_VIEW_ITEM_LIST!!.size - 1).forEach {
                val frag = IllustratorViewFragment().apply {
                    val bundle = Bundle()
                    bundle.putInt(IllustratorViewFragment.CLICKED_ITEM_INDEX, it)
                    if (it == FirstShowIndex) {
                        bundle.putBoolean(IllustratorViewFragment.SHOULD_FETCH_DRAWABLE, true) //放置可获取Drawable标记
                    }
                    arguments = bundle
                }
                ItemPages.add(frag)
            }
        }
        Illustrator2_Item_Pager.addOnPageChangeListener(object : BaseOnPageChangeListener() {
            override fun onPageSelected(p0: Int) {
                val imageView =
                    ItemPages[CURRENT_SHOW_IMAGE_INDEX].view?.findViewById<ImageView>(R.id.Illustrator2_View_ItemImageView)
                imageView?.transitionName = null
                CURRENT_SHOW_IMAGE_INDEX = p0
            }
        })
        Illustrator2_Item_Pager.adapter = IllustratorViewPageFragmentAdapter(ItemPages, supportFragmentManager)
        Illustrator2_Item_Pager.offscreenPageLimit = 5
        Illustrator2_Item_Pager.currentItem = FirstShowIndex
    }

    override fun AskForViewModel(fragmentIndex: Int, self: IllustratorViewFragment) {
        val model = ItemPageViewModelCache.get(fragmentIndex, null)
        if (model != null) {
            self.ApplyViewModel(model)
        } else if (CAN_VIEW_ITEM_LIST != null) {
            // Load model from network.
            LoadRecommendImageDetailData(fragmentIndex, CAN_VIEW_ITEM_LIST!![fragmentIndex])
        }
    }

    override fun AskForViewModel(fragmentIndex: Int, relatedImageInfo: RelatedImageInfo) {

        val model = RecommendItemModelImage().apply {
            ArtistName = relatedImageInfo.Artist
            URL = relatedImageInfo.URL
        }

        LoadRecommendImageDetailData(fragmentIndex, model)
    }

    // 供给Fragment调用
    override fun HandleDownloadImage(ImageURL: String, Title: String) {
        var Ext = ImageURL.substring(ImageURL.lastIndexOf('.'))
        if (Ext.isEmpty()) {
            Ext = ".png"
        }
        downloader.DownloadImage(
            Title + Ext,
            ImageURL,
            CheckPermissionModel(this)
        ) { absPath ->
            // Try update library.
            AppUtil.NotifyGalleryUpdate(this, File(absPath))
            runOnUiThread { Toast.makeText(this, "Download Finished!", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun LoadRecommendImageDetailData(needFragmentIndex: Int, content: RecommendItemModelImage) {
        ShowLineLoadingIndicator()
        // 第一阶段创建ViewModel， 放到正在Loading的Cache中。
        val model = IllustratorViewFragmentViewModel()

        model.apply {
            ArtistName = content.ArtistName
        }

        val url = HTMLParser.WrapDomain(content.URL)

        model.BrowserPageUrl = url
        LoadingItemPageViewModel.put(needFragmentIndex, model)

        DaggerFetchFactory
            .create()
            .fetcher()
            .visit(url)
            .goAsync(
                { response ->
                    response.handle<String>(
                        { htmlString ->
                            ParseImageItemDetailData(needFragmentIndex, htmlString)
                        },
                        { code, _ ->
                            runOnUiThread { Toast.makeText(this, code, Toast.LENGTH_SHORT).show() }
                        }
                    )
                },
                { e ->
                    runOnUiThread { LoadFailedTips(-4, e.message ?: "") }
                }
            )
    }

    private fun ParseImageItemDetailData(needFragmentIndex: Int, HTMLString: String) {

        // 从Cache中提取Model，继续填充信息
        val model = LoadingItemPageViewModel.get(needFragmentIndex)

        val root = Jsoup.parse(HTMLString)
        val StepsImageContent = htmlParser.Rules.getJSONObject("ImageContent").getJSONArray("Steps")
        val StepsRelatedImage = htmlParser.Rules.getJSONObject("RelatedImage").getJSONArray("Steps")

        try {
            val ImageContents = htmlParser.Parser.GoSteps(root, StepsImageContent) as ImageContentInfo
            val relatedItems =
                (htmlParser.Parser.GoSteps(root, StepsRelatedImage) as Array<*>).map { it as RelatedImageInfo }

            model.apply {
                ArtistAvatarUrl = HTMLParser.GetAlbumThumb(ImageContents.ArtistAvatar)
                Title = ImageContents.Title
                CreateDescription = ImageContents.CreateDescription.replace("<br>".toRegex(), "\n")
                CreateDetailRaw = ImageContents.CreateDetail
                ItemImageUrl = HTMLParser.GetAlbumThumb(ImageContents.URL)
                RelatedItems = relatedItems
            }
            synchronized(ItemPages) {
                if (ItemPages.isNotEmpty()) {
                    // 放到加载完的Cache中
                    ItemPageViewModelCache.put(needFragmentIndex, model)
                    // 从正在加载的Cache中移除
                    LoadingItemPageViewModel.delete(needFragmentIndex)
                    runOnUiThread {
                        HideLineLoadingIndicator()
                        ItemPages[needFragmentIndex].ApplyViewModel(model)
                    }
                }
            }

        } catch (e: Exception) {
            errorHandler.Handle(e)
        }
    }

    override fun HandleClose() {
        val intent = Intent()
        intent.putExtra("CURRENT_INDEX", CURRENT_SHOW_IMAGE_INDEX)
        setResult(REENTER_RESULT_CODE, intent)
        supportFinishAfterTransition()
    }

    override fun onBackPressed() {
        // 支持带共享元素动画的返回。
        HandleClose()
    }
}