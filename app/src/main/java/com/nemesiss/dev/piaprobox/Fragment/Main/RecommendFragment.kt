package com.nemesiss.dev.piaprobox.Fragment.Main

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nemesiss.dev.HTMLContentParser.InvalidStepExecutorException
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModel
import com.nemesiss.dev.piaprobox.Adapter.RecommendPage.RecommendItemAdapter
import com.nemesiss.dev.piaprobox.Application.PiaproboxApplication
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.HTMLParser
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.DaggerFetchFactory
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.SimpleResponseHandler
import com.nemesiss.dev.piaprobox.View.Common.LoadingIndicatorView
import kotlinx.android.synthetic.main.fragment_header.*
import kotlinx.android.synthetic.main.recommand_fragment.*
import org.jsoup.Jsoup
import java.lang.Exception

class RecommendFragment : BaseMainFragment() {

    private lateinit var htmlParser: HTMLParser

    private var recommendListAdapter : RecommendItemAdapter? = null
    private var recommendItemLayoutManager : LinearLayoutManager? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.recommand_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Refresh()
    }

    override fun Refresh() {
        ShowLoadingIndicator()
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
                    }, { code,_ ->
                        HideLoadingIndicator()
                        LoadFailedTips(code, resources.getString(R.string.Error_Page_Load_Failed))})
            }, { HideLoadingIndicator() })
    }

    private fun ParseContent(HTMLString : String) {

        val root = Jsoup.parse(HTMLString)
        val rule = htmlParser.Rules.getJSONObject("RecommendList").getJSONArray("Steps")
        try {
            val ContentModels = (htmlParser.Parser.GoSteps(root, rule) as Array<*>).map { it as RecommendItemModel }
            activity?.runOnUiThread {
                if(recommendItemLayoutManager == null) {
                    recommendItemLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    Recommend_Frag_RecyclerView.layoutManager = recommendItemLayoutManager
                }
                if(recommendListAdapter == null) {
                    recommendListAdapter = RecommendItemAdapter(ContentModels, context!!)
                    Recommend_Frag_RecyclerView.adapter = recommendListAdapter
                }
                else {
                    recommendListAdapter?.items = ContentModels
                    recommendListAdapter?.notifyDataSetChanged()
                }
            }
        }
        catch (e : InvalidStepExecutorException) {
            LoadFailedTips(-1,"InvalidStepExecutorException: ${e.message}")
        }
        catch (e : ClassNotFoundException) {
            LoadFailedTips(-2,"ClassNotFoundException: ${e.message}")
        }
        catch (e : Exception) {
            LoadFailedTips(-3,"Exception: ${e.message}")
        } finally {
            HideLoadingIndicator()
        }
    }
}