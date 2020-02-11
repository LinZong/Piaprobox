package com.nemesiss.dev.piaprobox.Activity.Image

import android.app.SharedElementCallback
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.transition.ChangeBounds
import android.transition.ChangeClipBounds
import android.transition.ChangeTransform
import android.transition.TransitionSet
import android.util.Log
import android.util.Range
import android.util.SparseArray
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.nemesiss.dev.HTMLContentParser.InvalidStepExecutorException
import com.nemesiss.dev.HTMLContentParser.Model.ImageContentInfo
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModelImage
import com.nemesiss.dev.piaprobox.Activity.Common.PiaproboxBaseActivity
import com.nemesiss.dev.piaprobox.Adapter.IllustratorPage.IllustratorViewPageFragmentAdapter
import com.nemesiss.dev.piaprobox.Fragment.Image.IllustratorViewFragment
import com.nemesiss.dev.piaprobox.Fragment.Recommend.MainRecommendFragment
import com.nemesiss.dev.piaprobox.Model.CheckPermissionModel
import com.nemesiss.dev.piaprobox.Model.Image.IllustratorViewFragmentViewModel
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.AsyncExecutor
import com.nemesiss.dev.piaprobox.Service.DaggerFactory.DaggerAsyncExecutorFactory
import com.nemesiss.dev.piaprobox.Service.DaggerFactory.DaggerDownloadServiceFactory
import com.nemesiss.dev.piaprobox.Service.DaggerFactory.DaggerHTMParserFactory
import com.nemesiss.dev.piaprobox.Service.DaggerModules.DownloadServiceModules
import com.nemesiss.dev.piaprobox.Service.DaggerModules.HTMLParserModules
import com.nemesiss.dev.piaprobox.Service.Download.DownloadService
import com.nemesiss.dev.piaprobox.Service.HTMLParser
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.DaggerFetchFactory
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.SimpleResponseHandler
import com.nemesiss.dev.piaprobox.Util.AppUtil
import kotlinx.android.synthetic.main.illustrator_view_activity2.*
import org.jsoup.Jsoup
import java.io.File
import javax.inject.Inject

class IllustratorViewActivity2 : PiaproboxBaseActivity() {
    companion object {

        @JvmStatic
        val RETEEN_RESULT_CODE = 6789;

        @JvmStatic
        val CLICKED_ITEM_INDEX = "CLICKED_ITEM_INDEX"

        @JvmStatic
        val IMAGE_PRE_SHOWN_IMAGE_INTENT_KEY = "IMAGE_PRE_SHOWN_IMAGE_INTENT_KEY"

        @JvmStatic
        var CAN_VIEW_ITEM_LIST: List<RecommendItemModelImage>? = null
            private set

        @JvmStatic
        var PRE_SHOWN_IMAGE: Drawable? = null
            private set

        @JvmStatic
        @Synchronized
        fun SetItemList(list: List<RecommendItemModelImage>) {
            CAN_VIEW_ITEM_LIST = list
        }

        @JvmStatic
        @Synchronized
        fun SetPreShownDrawable(drawable: Drawable) {
            PRE_SHOWN_IMAGE = drawable
        }
    }

    @Inject
    lateinit var htmlParser: HTMLParser

    @Inject
    lateinit var asyncExecutor: AsyncExecutor

    @Inject
    lateinit var downloader: DownloadService

    // 状态相关变量
    private var ItemPages = ArrayList<IllustratorViewFragment>()

    private var ItemPageViewModelCache = SparseArray<IllustratorViewFragmentViewModel>()

    private var LoadingItemPageViewModel = SparseArray<IllustratorViewFragmentViewModel>()


    private var CURRENT_SHOW_IMAGE_INDEX = 0

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

        val ClickedIndex = intent.getIntExtra(CLICKED_ITEM_INDEX, 0)
        // 把Fragment加载进来


        supportPostponeEnterTransition()
        InitFragmentPager(ClickedIndex)
    }

    private fun TellImageIsStillPreparing() {
        Toast.makeText(this, R.string.ImageContentInfoIsPreparing, Toast.LENGTH_SHORT).show()
    }

    private fun InitFragmentPager(FirstShowIndex: Int) {
        if (CAN_VIEW_ITEM_LIST != null) {

            IntRange(0, CAN_VIEW_ITEM_LIST!!.size - 1).forEach {
                val frag = IllustratorViewFragment().apply {
                    val bundle = Bundle()
                    bundle.putInt("MyIndex", it)
                    if (it == FirstShowIndex) {
                        bundle.putBoolean("FetchDrawable", true) //放置可获取Drawable标记
                    }
                    arguments = bundle
                }
                ItemPages.add(frag)
            }
        }
        Illustrator2_Item_Pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener
        {
            override fun onPageScrollStateChanged(p0: Int) {

            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
            }

            override fun onPageSelected(p0: Int) {
                Log.d("Illustrator2","清除上一个可见性")
                val imageView = ItemPages[CURRENT_SHOW_IMAGE_INDEX].view?.findViewById<ImageView>(R.id.Illustrator2_View_ItemImageView)
                imageView?.transitionName = null
                CURRENT_SHOW_IMAGE_INDEX = p0
            }

        })
        Illustrator2_Item_Pager.adapter = IllustratorViewPageFragmentAdapter(ItemPages, supportFragmentManager)
        Illustrator2_Item_Pager.offscreenPageLimit = 5
        Illustrator2_Item_Pager.currentItem = FirstShowIndex
    }


    fun AskForViewModel(fragmentIndex: Int, self: IllustratorViewFragment) {
        val model = ItemPageViewModelCache.get(fragmentIndex, null)
        if (model != null) {
            self.ApplyViewModel(model)
        } else if (CAN_VIEW_ITEM_LIST != null) {
            // Load model from network.
            LoadRecommendImageDetailData(fragmentIndex, CAN_VIEW_ITEM_LIST!![fragmentIndex])
        }
    }


    // 供给Fragment调用
    fun HandleDownloadImage(ImageURL: String, Title: String) {

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

        // 第一阶段创建ViewModel， 放到正在Loading的Cache中。
        val model = IllustratorViewFragmentViewModel()

        model.ArtistAvatarUrl = HTMLParser.GetAlbumThumb(content.ArtistAvatar)
        model.ArtistName = content.ArtistName

        val url = MainRecommendFragment.DefaultTagUrl + content.URL

        model.BrowserPageUrl = url
        LoadingItemPageViewModel.put(needFragmentIndex, model)

        DaggerFetchFactory
            .create()
            .fetcher()
            .visit(url)
            .goAsync(
                {
                    SimpleResponseHandler(it, String::class)
                        .Handle(
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
        val Steps = htmlParser.Rules.getJSONObject("ImageContent").getJSONArray("Steps")

        try {
            val DetailContent = htmlParser.Parser.GoSteps(root, Steps) as ImageContentInfo

            model.Title = DetailContent.Title
            model.CreateDescription = DetailContent.CreateDescription.replace("<br>".toRegex(), "\n")
            model.CreateDetailRaw = DetailContent.CreateDetail
            model.ItemImageUrl = HTMLParser.GetAlbumThumb(DetailContent.URL)

            // 放到加载完的Cache中
            ItemPageViewModelCache.put(needFragmentIndex, model)
            // 从正在加载的Cache中移除
            LoadingItemPageViewModel.delete(needFragmentIndex)
            runOnUiThread {
                ItemPages[needFragmentIndex].ApplyViewModel(model)
            }

        } catch (e: InvalidStepExecutorException) {
            LoadFailedTips(-1, "InvalidStepExecutorException: ${e.message}")
        } catch (e: ClassNotFoundException) {
            LoadFailedTips(-2, "ClassNotFoundException: ${e.message}")
        } catch (e: Exception) {
            LoadFailedTips(-3, "Exception: ${e.message}")
        } finally {

        }
    }

    fun HandleClose() {
        val intent = Intent()
        intent.putExtra("CURRENT_INDEX", CURRENT_SHOW_IMAGE_INDEX)
        setResult(RETEEN_RESULT_CODE,intent)
        supportFinishAfterTransition()
    }

    override fun onBackPressed() {
        // 支持带共享元素动画的返回。
        HandleClose()
    }

}