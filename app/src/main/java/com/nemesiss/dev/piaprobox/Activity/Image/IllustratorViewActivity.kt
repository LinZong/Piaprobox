package com.nemesiss.dev.piaprobox.Activity.Image

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
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
import com.nemesiss.dev.piaprobox.Fragment.Recommend.MainRecommendFragment
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.AsyncExecutor
import com.nemesiss.dev.piaprobox.Service.DaggerFactory.DaggerAsyncExecutorFactory
import com.nemesiss.dev.piaprobox.Service.DaggerFactory.DaggerHTMParserFactory
import com.nemesiss.dev.piaprobox.Service.DaggerModules.HTMLParserModules
import com.nemesiss.dev.piaprobox.Service.HTMLParser
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.DaggerFetchFactory
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.SimpleResponseHandler
import kotlinx.android.synthetic.main.illustrator_view_activity.*
import org.jsoup.Jsoup
import javax.inject.Inject

class IllustratorViewActivity : PiaproboxBaseActivity() {

    companion object {
        @JvmStatic
        val IMAGE_DETAIL_INTENT_KEY = "IMAGE_DETAIL_INTENT_KEY"

        @JvmStatic
        val IMAGE_PRE_SHOWN_IMAGE_INTENT_KEY = "IMAGE_PRE_SHOWN_IMAGE_INTENT_KEY"

        @JvmStatic
        var PRE_SHOWN_BITMAP : Drawable? = null
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.illustrator_view_activity)

        DaggerHTMParserFactory
            .builder()
            .hTMLParserModules(HTMLParserModules(this))
            .build()
            .inject(this)

        DaggerAsyncExecutorFactory.builder().hTMLParserModules(HTMLParserModules(this)).build().inject(this)

        val data = intent.getSerializableExtra(IMAGE_DETAIL_INTENT_KEY) as? RecommendItemModelImage
        if (data != null) {
            LoadRecommendImageDetailData(data)
        }
        Illustrator_View_BackButton.setOnClickListener { onBackPressed() }
        Illustrator_View_ItemImageView.setImageDrawable(PRE_SHOWN_BITMAP)
    }

    private fun LoadRecommendImageDetailData(content: RecommendItemModelImage) {
        Glide.with(this)
            .load(HTMLParser.GetAlbumThumb(content.ArtistAvatar))
            .into(Illustrator_View_ArtistAvatar)

        Illustrator_View_ArtistName.text = content.ArtistName
        val url = MainRecommendFragment.DefaultTagUrl + content.URL

        DaggerFetchFactory
            .create()
            .fetcher()
            .visit(url)
            .goAsync(
                { SimpleResponseHandler(it, String::class)
                    .Handle(
                        { htmlString ->
                            ParseImageItemDetailData(htmlString)
                        },
                        {
                            code,_ ->
                            runOnUiThread { Toast.makeText(this, code, Toast.LENGTH_SHORT).show() }
                        }
                    )},
                { e ->
                    runOnUiThread { LoadFailedTips(-4, e.message ?: "") }
                }
            )
    }

    private fun HideFrontImageView() {
        if(Illustrator_View_ItemImageView.visibility != View.GONE) {
            asyncExecutor.SendTaskMainThreadDelay(Runnable {
                Illustrator_View_ItemImageView.visibility = View.GONE
                Illustrator_View_ItemImageView.setImageDrawable(Illustrator_View_ItemImageView_Back.drawable)
            },100)
        }
    }

    private fun ParseImageItemDetailData(HTMLString : String) {
        val root = Jsoup.parse(HTMLString)
        val Steps = htmlParser.Rules.getJSONObject("ImageContent").getJSONArray("Steps")

        try {
            val DetailContent = htmlParser.Parser.GoSteps(root, Steps) as ImageContentInfo
            val albUrl = HTMLParser.GetAlbumThumb(DetailContent.URL)
            Log.d("IllustratorViewActivity", albUrl)
            runOnUiThread {
                Glide.with(this)
                    .load(albUrl)
                    .priority(Priority.HIGH)
                    .dontAnimate()
                    .dontTransform()
                    .listener(object : RequestListener<String, GlideDrawable>
                    {
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

                    })
                    .into(Illustrator_View_ItemImageView_Back)
                Illustrator_View_ItemDetail.text = DetailContent.CreateDescription
                Illustrator_View_ItemName.text = DetailContent.Title
            }

        }
        catch (e: InvalidStepExecutorException) {
            LoadFailedTips(-1, "InvalidStepExecutorException: ${e.message}")
        } catch (e: ClassNotFoundException) {
            LoadFailedTips(-2, "ClassNotFoundException: ${e.message}")
        } catch (e: Exception) {
            LoadFailedTips(-3, "Exception: ${e.message}")
        } finally {

        }
    }

    private fun HandleClose() {
        if(Illustrator_View_ItemImageView.visibility==View.GONE) {
            Illustrator_View_ItemImageView.visibility = View.VISIBLE
        }
        supportFinishAfterTransition()
    }
    override fun onBackPressed() {
        // 支持带共享元素动画的返回。
        HandleClose()
    }
}