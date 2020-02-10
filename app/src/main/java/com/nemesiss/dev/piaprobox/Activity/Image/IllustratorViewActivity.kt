package com.nemesiss.dev.piaprobox.Activity.Image

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.nemesiss.dev.HTMLContentParser.InvalidStepExecutorException
import com.nemesiss.dev.HTMLContentParser.Model.ImageContentInfo
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModelImage
import com.nemesiss.dev.piaprobox.Activity.Common.PiaproboxBaseActivity
import com.nemesiss.dev.piaprobox.Activity.Common.PreviewImageActivity
import com.nemesiss.dev.piaprobox.Fragment.Recommend.MainRecommendFragment
import com.nemesiss.dev.piaprobox.Model.CheckPermissionModel
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
import com.nemesiss.dev.piaprobox.View.Common.AutoWrapLayout
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

    @Inject
    lateinit var asyncExecutor: AsyncExecutor

    @Inject
    lateinit var downloader: DownloadService

    private var CurremtImageRecommendInfo : RecommendItemModelImage? = null

    private var CurrentImageContentInfo: ImageContentInfo? = null

    private var CurrentImageDownloadTokens : ArrayList<Pair<String,String>>? = null

    private val LoadOriginalWorkImageListener = object : RequestListener<String, GlideDrawable> {
        override fun onException(
            e: java.lang.Exception?,
            model: String?,
            target: Target<GlideDrawable>?,
            isFirstResource: Boolean
        ): Boolean {
            HideFrontImageView()
            return false
        }

        override fun onResourceReady(
            resource: GlideDrawable?,
            model: String?,
            target: Target<GlideDrawable>?,
            isFromMemoryCache: Boolean,
            isFirstResource: Boolean
        ): Boolean {
            HideFrontImageView()
            return false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.illustrator_view_activity)

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

        val data = intent.getSerializableExtra(IMAGE_DETAIL_INTENT_KEY) as? RecommendItemModelImage
        if (data != null) {
            LoadRecommendImageDetailData(data)
        }
        BindButtons()
    }

    private fun BindButtons() {
        Illustrator_View_BackButton.setOnClickListener { onBackPressed() }
        Illustrator_View_ItemImageView.setImageDrawable(PRE_SHOWN_BITMAP)
        Illustrator_View_DownloadImage.setOnClickListener { HandleDownloadImage() }
        Illustrator_View_ItemImageView_Back.setOnClickListener { HandleEnterPinchImagePreview() }
        Illustrator_View_OpenBrowser.setOnClickListener {
            if(CurremtImageRecommendInfo!=null) {
                AppUtil.OpenBrowser(this, MainRecommendFragment.DefaultTagUrl + CurremtImageRecommendInfo!!.URL)
            }
        }
    }

    private val IsCurrentImageLoaded
        get() = CurrentImageContentInfo != null

    private fun HandleEnterPinchImagePreview() {
        if(!IsCurrentImageLoaded || Illustrator_View_ItemImageView_Back.drawable == null) {
            TellImageIsStillPreparing()
            return
        }

        PreviewImageActivity.SetPreShownDrawable(Illustrator_View_ItemImageView_Back.drawable)
        startActivity(Intent(this, PreviewImageActivity::class.java))
    }

    private fun TellImageIsStillPreparing() {
        Toast.makeText(this, R.string.ImageContentInfoIsPreparing, Toast.LENGTH_SHORT).show()
    }

    private fun HandleDownloadImage() {
        if (!IsCurrentImageLoaded) {
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

        CurremtImageRecommendInfo = null
        CurrentImageContentInfo = null // 清空掉当前显示的图片的信息
        CurrentImageDownloadTokens = null

        Glide.with(this)
            .load(HTMLParser.GetAlbumThumb(content.ArtistAvatar))
            .into(Illustrator_View_ArtistAvatar)

        CurremtImageRecommendInfo = content

        Illustrator_View_ArtistName.text = content.ArtistName
        val url = MainRecommendFragment.DefaultTagUrl + content.URL

        DaggerFetchFactory
            .create()
            .fetcher()
            .visit(url)
            .goAsync(
                {
                    SimpleResponseHandler(it, String::class)
                        .Handle(
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

    private fun HideFrontImageView() {
        if (Illustrator_View_ItemImageView.visibility != View.GONE) {
            asyncExecutor.SendTaskMainThreadDelay(Runnable {
                Illustrator_View_ItemImageView.visibility = View.GONE
                Illustrator_View_ItemImageView.setImageDrawable(Illustrator_View_ItemImageView_Back.drawable)
            }, 100)
        }
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
        if(downloadRoot==null) {
            downloadRoot = root.getElementById("_form_download_bookmark")
        }
        val inputs = downloadRoot.getElementsByTag("input")
        for(input in inputs) {
            downloadTokens.add(Pair(input.attr("name"),input.attr("value")))
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
            runOnUiThread {
                Glide.with(this)
                    .load(albUrl)
                    .priority(Priority.HIGH)
                    .dontAnimate()
                    .dontTransform()
                    .listener(LoadOriginalWorkImageListener)
                    .into(Illustrator_View_ItemImageView_Back)
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
        if (Illustrator_View_ItemImageView.visibility == View.GONE) {
            Illustrator_View_ItemImageView.visibility = View.VISIBLE
        }
        supportFinishAfterTransition()
    }

    override fun onBackPressed() {
        // 支持带共享元素动画的返回。
        HandleClose()
    }
}