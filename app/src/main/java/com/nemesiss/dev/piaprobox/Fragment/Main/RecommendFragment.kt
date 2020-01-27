package com.nemesiss.dev.piaprobox.Fragment.Main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nemesiss.dev.HTMLContentParser.InvalidStepExecutorException
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModel
import com.nemesiss.dev.piaprobox.Application.PiaproboxApplication
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.HTMLParser
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.DaggerFetchFactory
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.FetchFactory
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.SimpleResponseHandler
import com.nemesiss.dev.piaprobox.View.Common.LoadingIndicatorView
import kotlinx.android.synthetic.main.fragment_header.*
import org.jsoup.Jsoup
import java.lang.Exception
import java.util.*

class RecommendFragment : BaseMainFragment() {

    private lateinit var htmlParser: HTMLParser

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.recommand_fragment, container, false)
    }

    override fun Refresh() {
//        val topView = activity?.findViewById<ViewGroup>(android.R.id.content)
//        topView?.addView(LoadingIndicatorView(context))

        LoadContent()
    }

    override fun LoadBannerImage() {
        Log.d("RecommendFragment", BaseMainFragment_Banner_ImageView.toString())
        BaseMainFragment_Banner_ImageView.setImageResource(R.drawable.recommand_banner)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        htmlParser = HTMLParser(context ?: PiaproboxApplication.Self.applicationContext)
    }

    private fun LoadContent() {
        DaggerFetchFactory.create()
            .fetcher()
            .visit("https://piapro.jp/")
            .goAsync({ response ->
                SimpleResponseHandler(response, String::class)
                    .Handle({
                        ParseContent(it as String)
                    }, { code,_ -> LoadFailedTips(code, resources.getString(R.string.Error_Page_Load_Failed))})
            }, {})
    }

    private fun ParseContent(HTMLString : String) {

        val root = Jsoup.parse(HTMLString)
        val rule = htmlParser.Rules.getJSONObject("RecommendList").getJSONArray("Steps")
        try {
            val Contents = (htmlParser.Parser.GoSteps(root, rule) as Array<*>).map { it as RecommendItemModel }

        }
        catch (e : InvalidStepExecutorException) {

        }
        catch (e : ClassNotFoundException) {

        }
        catch (e : Exception) {

        }
    }
}