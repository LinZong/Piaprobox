package com.nemesiss.dev.piaprobox.Activity.Text

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModelText
import com.nemesiss.dev.HTMLContentParser.Model.TextContentInfo
import com.nemesiss.dev.piaprobox.Activity.Common.PiaproboxBaseActivity
import com.nemesiss.dev.piaprobox.Model.Text.TextDetailActivityViewModel
import com.nemesiss.dev.piaprobox.Service.DaggerFactory.DaggerErrorHandlerFactory
import com.nemesiss.dev.piaprobox.Service.DaggerFactory.DaggerHtmlParserFactory
import com.nemesiss.dev.piaprobox.Service.DaggerModules.ErrorHandlerModules
import com.nemesiss.dev.piaprobox.Service.DaggerModules.HtmlParserModules
import com.nemesiss.dev.piaprobox.Service.GlobalErrorHandler.ParseContentErrorHandler
import com.nemesiss.dev.piaprobox.Service.HTMLParser
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.DaggerFetchFactory
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.handle
import com.nemesiss.dev.piaprobox.databinding.TextDetailActivityBinding
import kotlinx.android.synthetic.main.text_detail_activity.*
import org.jsoup.Jsoup
import javax.inject.Inject

class TextDetailActivity : PiaproboxBaseActivity() {

    companion object {
        @JvmStatic
        val SHOWN_TEXT_INTENT_KEY = "SHOWN_TEXT_INTENT_KEY"
    }


    @Inject
    lateinit var htmlParser: HTMLParser

    lateinit var errorHandler: ParseContentErrorHandler


    private var CuurentTextContentInfo: TextContentInfo? = null

    private lateinit var binding: TextDetailActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DaggerHtmlParserFactory
            .builder()
            .htmlParserModules(HtmlParserModules(this))
            .build()
            .inject(this)

        errorHandler =
            DaggerErrorHandlerFactory
                .builder()
                .errorHandlerModules(ErrorHandlerModules(this))
                .build()
                .handler()

        // Create data binding.
        binding = TextDetailActivityBinding.inflate(layoutInflater)

        setContentView(binding.root)

        ShowToolbarBackIcon(TextDetail_Toolbar)

        val data = intent.getSerializableExtra(SHOWN_TEXT_INTENT_KEY) as? RecommendItemModelText
        if (data != null) {
            LoadTextContent(data)
        } else {
            // TODO Show no text content loaded.
        }
    }

    private fun LoadTextContent(content: RecommendItemModelText) {
        DaggerFetchFactory
            .create()
            .fetcher()
            .visit(HTMLParser.wrapDomain(content.URL))
            .goAsync(
                { response ->
                    response.handle<String>(
                        { htmlString ->
                            ParseTextDetailContent(htmlString)
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
    private fun ParseTextDetailContent(HTMLString: String) {
        try {
            val root = Jsoup.parse(HTMLString)
            val Steps = htmlParser.Rules.getJSONObject("TextContent").getJSONArray("Steps")
            val textContentInfo = htmlParser.Parser.GoSteps(root, Steps) as TextContentInfo

            CuurentTextContentInfo = textContentInfo

            val viewModel = TextDetailActivityViewModel().apply {
                ArtistName = textContentInfo.Artist
                ArtistAvatarUrl = HTMLParser.GetAlbumThumb(textContentInfo.ArtistAvatar)
                Titie = textContentInfo.Title
                CreateDescription = textContentInfo.CreateDescription
                CreateDetailRaw = textContentInfo.CreateDetail
                Text = textContentInfo.Text.replace("<br> ", "\n")
            }

            binding.model = viewModel

        } catch (e: Exception) {
            errorHandler.Handle(e)
        } finally {

        }
    }

    fun SmallerFont(view: View) {
//        TextDetail_Content.setTextSize(TypedValue.COMPLEX_UNIT_PX, TextDetail_Content.textSize-1)
    }

    fun BiggerFont(view: View) {
//        TextDetail_Content.setTextSize(TypedValue.COMPLEX_UNIT_PX, TextDetail_Content.textSize+1)
    }
}