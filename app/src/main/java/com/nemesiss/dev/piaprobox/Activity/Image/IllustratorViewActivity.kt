package com.nemesiss.dev.piaprobox.Activity.Image

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.nemesiss.dev.contentparser.InvalidStepExecutorException
import com.nemesiss.dev.contentparser.model.ImageContentInfo
import com.nemesiss.dev.contentparser.model.RecommendItemModelImage
import com.nemesiss.dev.piaprobox.Activity.Common.PiaproboxBaseActivity
import com.nemesiss.dev.piaprobox.Activity.Common.PreviewImageActivity
import com.nemesiss.dev.piaprobox.model.CheckPermissionModel
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.AsyncExecutor
import com.nemesiss.dev.piaprobox.Service.DaggerFactory.DaggerDownloadServiceFactory
import com.nemesiss.dev.piaprobox.Service.DaggerFactory.DaggerHtmlParserFactory
import com.nemesiss.dev.piaprobox.Service.DaggerModules.DownloadServiceModules
import com.nemesiss.dev.piaprobox.Service.DaggerModules.HtmlParserModules
import com.nemesiss.dev.piaprobox.Service.Download.DownloadService
import com.nemesiss.dev.piaprobox.Service.HTMLParser
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.DaggerFetchFactory
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.handle
import com.nemesiss.dev.piaprobox.util.AppUtil
import com.nemesiss.dev.piaprobox.view.common.AutoWrapLayout
import com.nemesiss.dev.piaprobox.view.common.whenClicks
import kotlinx.android.synthetic.main.illustrator_view_activity.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import javax.inject.Inject

class IllustratorViewActivity : PiaproboxBaseActivity() {

    companion object {
        @JvmStatic
        val IMAGE_DETAIL_INTENT_KEY = "IMAGE_DETAIL_INTENT_KEY"

        @JvmStatic
        val IMAGE_PRE_SHOWN_IMAGE_INTENT_KEY = "IMAGE_PRE_SHOWN_IMAGE_INTENT_KEY"

        @JvmStatic
        var PRE_SHOWN_BITMAP: Drawable? = null
            @Synchronized
            get() {
                val last = field
                field = null
                return last
            }
            private set

        @JvmStatic
        @Synchronized
        fun SetPreShownDrawable(drawable: Drawable) {
            PRE_SHOWN_BITMAP = drawable
        }
    }

    @Inject
    lateinit var htmlParser: HTMLParser

    private var asyncExecutor: AsyncExecutor = AsyncExecutor.INSTANCE

    @Inject
    lateinit var downloader: DownloadService

    // 状态相关变量

    private var CurremtImageRecommendInfo: RecommendItemModelImage? = null

    private var CurrentImageContentInfo: ImageContentInfo? = null

    private var CurrentImageDownloadTokens: ArrayList<Pair<String, String>>? = null

    private var IS_CURRENT_BIG_SIZE_IMAGE_LOADED = false

    private val IS_CURRENT_IMAGE_INFO_LOADED
        get() = CurrentImageContentInfo != null

    // 监听器

    private val LoadOriginalWorkDrawableToImageViewListener = object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            IS_CURRENT_BIG_SIZE_IMAGE_LOADED = false
            return true
        }
        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            if (isFirstResource) {
                return false // thumbnail was not shown, do as usual
            }
            IS_CURRENT_BIG_SIZE_IMAGE_LOADED = true
            Illustrator_View_ItemImageView.setImageDrawable(resource)
            return true
        }
    }


    private fun ResetCurrentImageInfos() {
        CurrentImageDownloadTokens = null
        CurrentImageContentInfo = null
        CurremtImageRecommendInfo = null
        IS_CURRENT_BIG_SIZE_IMAGE_LOADED = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.illustrator_view_activity)

        Illustrator_View_ItemImageView.setImageDrawable(PRE_SHOWN_BITMAP)
//
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

        val data = intent.getSerializableExtra(IMAGE_DETAIL_INTENT_KEY) as? RecommendItemModelImage
        if (data != null) {
            LoadRecommendImageDetailData(data)
        }
        BindButtons()
    }

    private fun BindButtons() {
        listOf(
            Illustrator_View_BackButton,
            Illustrator_View_DownloadImage,
            Illustrator_View_ItemImageView,
            Illustrator_View_OpenBrowser
        ).whenClicks(
            { onBackPressed() },
            { HandleDownloadImage() },
            { HandleEnterPinchImagePreview() },
            {
                if (CurremtImageRecommendInfo != null) {
                    AppUtil.OpenBrowser(this, HTMLParser.wrapDomain(CurremtImageRecommendInfo!!.URL))
                }
            })
    }

    private fun HandleEnterPinchImagePreview() {
        if (!IS_CURRENT_BIG_SIZE_IMAGE_LOADED) {
            TellImageIsStillPreparing()
            return
        }
        PreviewImageActivity.SetPreShownDrawable(Illustrator_View_ItemImageView.drawable)
        startActivity(Intent(this, PreviewImageActivity::class.java))
    }

    private fun TellImageIsStillPreparing() {
        Toast.makeText(this, R.string.ImageContentInfoIsPreparing, Toast.LENGTH_SHORT).show()
    }

    private fun HandleDownloadImage() {
        if (!IS_CURRENT_IMAGE_INFO_LOADED) {
            TellImageIsStillPreparing()
            return
        }
        val ImageURL = HTMLParser.GetAlbumThumb(CurrentImageContentInfo!!.URL)
        var Ext = ImageURL.substring(ImageURL.lastIndexOf('.'))
        if (Ext.isEmpty()) {
            Ext = ".png"
        }
        downloader.DownloadImage(
            CurrentImageContentInfo!!.Title + Ext,
            ImageURL,
            CheckPermissionModel(this)
        ) { absPath ->
            // Try update library.
            AppUtil.NotifyGalleryUpdate(this, File(absPath))
            runOnUiThread { Toast.makeText(this, "Download Finished!", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun LoadRecommendImageDetailData(content: RecommendItemModelImage) {

        ResetCurrentImageInfos() // 清空表示当前图片的变量信息。

        Glide.with(this)
            .load(HTMLParser.GetAlbumThumb(content.ArtistAvatar))
            .into(Illustrator_View_ArtistAvatar)

        CurremtImageRecommendInfo = content

        Illustrator_View_ArtistName.text = content.ArtistName
        val url = HTMLParser.wrapDomain(content.URL)

        DaggerFetchFactory
            .create()
            .fetcher()
            .visit(url)
            .goAsync(
                { response ->
                    response.handle<String>(
                        { htmlString ->
                            ParseImageItemDetailData(htmlString)
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


    private fun ShowWorkItemInfo(OriginalWorkInfoText: String, AutoWrapContainer: AutoWrapLayout) {
        OriginalWorkInfoText.split(" | ").forEach {
            val DelimiterPos = it.indexOf('：')
            // Key: 0-DelimiterPos   Value: DelimiterPos+1-End
            val text = SpannableString(it)
            val tagColor = ForegroundColorSpan(resources.getColor(R.color.TagSelectedBackground))
            if (DelimiterPos > 1) {
                text.setSpan(tagColor, 0, DelimiterPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            val tv = TextView(this)
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PT, 6f)
            tv.text = text
            val lp =
                ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.setMargins(0, 0, 12, 8)
            tv.layoutParams = lp
            AutoWrapContainer.addView(tv)
        }
    }

    private fun ParseDownloadTokenData(root: Document) {
        val downloadTokens = ArrayList<Pair<String, String>>()
        var downloadRoot = root.getElementById("_form_download")
        if (downloadRoot == null) {
            downloadRoot = root.getElementById("_form_download_bookmark")
        }
        val inputs = downloadRoot.getElementsByTag("input")
        for (input in inputs) {
            downloadTokens.add(Pair(input.attr("name"), input.attr("value")))
        }
        CurrentImageDownloadTokens = downloadTokens
    }

    private fun ParseImageItemDetailData(HTMLString: String) {
        val root = Jsoup.parse(HTMLString)

//        ParseDownloadTokenData(root)

        val Steps = htmlParser.Rules.getJSONObject("ImageContent").getJSONArray("Steps")

        try {
            val DetailContent = htmlParser.Parser.GoSteps(root, Steps) as ImageContentInfo

            CurrentImageContentInfo = DetailContent

            val albUrl = HTMLParser.GetAlbumThumb(DetailContent.URL)
            Glide.with(this)
                .load(albUrl)
                .listener(LoadOriginalWorkDrawableToImageViewListener)
                .preload()

            runOnUiThread {
                Illustrator_View_ItemDetail.text = DetailContent.CreateDescription.replace("<br>".toRegex(), "\n")
                Illustrator_View_ItemName.text = DetailContent.Title
                ShowWorkItemInfo(DetailContent.CreateDetail, Illustrator_View_ItemInfoContainer)
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

    private fun HandleClose() {
        supportFinishAfterTransition()
    }

    override fun onBackPressed() {
        // 支持带共享元素动画的返回。
        HandleClose()
    }
}

