package com.nemesiss.dev.piaprobox.Activity.Image

import android.content.Intent
import android.os.Bundle
import android.util.SparseArray
import android.view.ViewGroup
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
import com.nemesiss.dev.piaprobox.Service.DaggerFactory.DaggerDownloadServiceFactory
import com.nemesiss.dev.piaprobox.Service.DaggerFactory.DaggerErrorHandlerFactory
import com.nemesiss.dev.piaprobox.Service.DaggerFactory.DaggerHtmlParserFactory
import com.nemesiss.dev.piaprobox.Service.DaggerModules.DownloadServiceModules
import com.nemesiss.dev.piaprobox.Service.DaggerModules.ErrorHandlerModules
import com.nemesiss.dev.piaprobox.Service.DaggerModules.HtmlParserModules
import com.nemesiss.dev.piaprobox.Service.Download.DownloadService
import com.nemesiss.dev.piaprobox.Service.GlobalErrorHandler.ParseContentErrorHandler
import com.nemesiss.dev.piaprobox.Service.HTMLParser
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.DaggerFetchFactory
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.handle
import com.nemesiss.dev.piaprobox.Util.AppUtil
import com.nemesiss.dev.piaprobox.Util.BaseOnPageChangeListener
import com.nemesiss.dev.piaprobox.Util.runWhenAlive
import com.nemesiss.dev.piaprobox.View.Common.LoadingLineIndicator
import com.nemesiss.dev.piaprobox.View.SharedElements.SharedElementUtils
import kotlinx.android.synthetic.main.illustrator_view_activity2.*
import org.jsoup.Jsoup
import java.io.File
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

    val asyncExecutor: AsyncExecutor = AsyncExecutor.INSTANCE

    @Inject
    lateinit var downloader: DownloadService

    lateinit var errorHandler: ParseContentErrorHandler

    // 状态相关变量
    private var ItemPages = ArrayList<IllustratorViewFragment>()

    private var ItemPageViewModelCache = SparseArray<IllustratorViewFragmentViewModel>()

    private var LoadingItemPageViewModel = SparseArray<IllustratorViewFragmentViewModel>()

    private var CURRENT_SHOW_IMAGE_INDEX = 0

    private var LoadingLineIndicatorInstance : LoadingLineIndicator? = null

    override fun onDestroy() {
        Illustrator2_Item_Pager.removeAllViews()
        ItemPageViewModelCache.clear()
        ItemPages.clear()
        super.onDestroy()
    }

    private fun ShowLineLoadingIndicator() {
        LoadingLineIndicatorInstance = LoadingLineIndicatorInstance ?: LoadingLineIndicator(this).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AppUtil.Dp2Px(resources, 12))
            Illustrator2_FrameLayout_ViewRoot.addView(this)
        }
    }

    private fun HideLineLoadingIndicator() {
        LoadingLineIndicatorInstance = LoadingLineIndicatorInstance?.let { me ->
            Illustrator2_FrameLayout_ViewRoot.removeView(me)
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.illustrator_view_activity2)

        // Dagger inject to activity.
        DaggerHtmlParserFactory
            .builder()
            .htmlParserModules(HtmlParserModules(this))
            .build()
            .inject(this)

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
        CAN_VIEW_ITEM_LIST?.indices?.forEach {
            ItemPages.add(IllustratorViewFragment().apply {
                val bundle = Bundle()
                bundle.putInt(IllustratorViewFragment.CLICKED_ITEM_INDEX, it)
                if (it == FirstShowIndex) {
                    bundle.putBoolean(IllustratorViewFragment.SHOULD_FETCH_DRAWABLE, true) //放置可获取Drawable标记
                }
                arguments = bundle
            })
        }
        Illustrator2_Item_Pager.addOnPageChangeListener(object : BaseOnPageChangeListener() {
            override fun onPageSelected(page: Int) {
                val imageView =
                    ItemPages[CURRENT_SHOW_IMAGE_INDEX].view?.findViewById<ImageView>(R.id.Illustrator2_View_ItemImageView)
                imageView?.transitionName = null
                CURRENT_SHOW_IMAGE_INDEX = page
            }
        })
        Illustrator2_Item_Pager.adapter = IllustratorViewPageFragmentAdapter(ItemPages, supportFragmentManager)
        Illustrator2_Item_Pager.offscreenPageLimit = 5
        Illustrator2_Item_Pager.currentItem = FirstShowIndex
    }

    override fun AskForViewModel(fragmentIndex: Int, self: IllustratorViewFragment) {
        val model = ItemPageViewModelCache.get(fragmentIndex, null)
        if (model != null) {
            self.ApplyViewModel(model) {
                HideLineLoadingIndicator()
            }
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
        var ext = ImageURL.substring(ImageURL.lastIndexOf('.'))
        if (ext.isEmpty()) {
            ext = ".png"
        }
        downloader.DownloadImage(
            Title + ext,
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

        val url = HTMLParser.wrapDomain(content.URL)

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
        val model = LoadingItemPageViewModel[needFragmentIndex]

        val root = Jsoup.parse(HTMLString)
        val parseImageInfoSteps = htmlParser.Rules.getJSONObject("ImageContent").getJSONArray("Steps")
        val parseRelatedImageSteps = htmlParser.Rules.getJSONObject("RelatedImage").getJSONArray("Steps")

        runWhenAlive {
            try {
                val imageInfo = htmlParser.Parser.GoSteps(root, parseImageInfoSteps) as ImageContentInfo
                val relatedItems =
                    (htmlParser.Parser.GoSteps(root, parseRelatedImageSteps) as Array<*>).map { it as RelatedImageInfo }

                model.apply {
                    ArtistName = imageInfo.Artist
                    ArtistAvatarUrl = HTMLParser.GetAlbumThumb(imageInfo.ArtistAvatar)
                    Title = imageInfo.Title
                    CreateDescription = imageInfo.CreateDescription.replace("<br>".toRegex(), "\n")
                    CreateDetailRaw = imageInfo.CreateDetail
                    ItemImageUrl = HTMLParser.GetAlbumThumb(imageInfo.URL)
                    RelatedItems = relatedItems
                }
                synchronized(ItemPages) {
                    if (ItemPages.isNotEmpty()) {
                        // 放到加载完的Cache中
                        ItemPageViewModelCache.put(needFragmentIndex, model)
                        // 从正在加载的Cache中移除
                        LoadingItemPageViewModel.delete(needFragmentIndex)
                        runOnUiThread {
                            ItemPages[needFragmentIndex].ApplyViewModel(model) {
                                HideLineLoadingIndicator()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                errorHandler.Handle(e)
            }
        }
    }

    override fun HandleClose() {
        val intent = Intent()
        intent.putExtra("CURRENT_INDEX", CURRENT_SHOW_IMAGE_INDEX)
        setResult(REENTER_RESULT_CODE, intent)
        SharedElementUtils.setPendingExitSharedElements(this, arrayListOf(resources.getString(R.string.ImageViewTransitionName)))
        supportFinishAfterTransition()
    }

    override fun onBackPressed() {
        // 支持带共享元素动画的返回。
        HandleClose()
    }
}