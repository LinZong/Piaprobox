package com.nemesiss.dev.piaprobox.Fragment.Main

import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nemesiss.dev.HTMLContentParser.InvalidStepExecutorException
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModel
import com.nemesiss.dev.HTMLContentParser.Model.RecommendTagModel
import com.nemesiss.dev.piaprobox.Adapter.Common.TagItemAdapter
import com.nemesiss.dev.piaprobox.Adapter.RecommendPage.RecommendItemAdapter
import com.nemesiss.dev.piaprobox.Application.PiaproboxApplication
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.HTMLParser
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.DaggerFetchFactory
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.SimpleResponseHandler
import com.nemesiss.dev.piaprobox.View.Common.SingleTagView
import kotlinx.android.synthetic.main.fragment_header.*
import kotlinx.android.synthetic.main.recommand_fragment.*
import org.jsoup.Jsoup
import java.lang.Exception

class RecommendFragment : BaseMainFragment() {

    private lateinit var htmlParser: HTMLParser

    private var recommendListAdapter: RecommendItemAdapter? = null
    private var recommendItemLayoutManager: LinearLayoutManager? = null
    private var recommendListData: List<RecommendItemModel>? = null

    private var tagListAdapter: TagItemAdapter? = null
    private var tagListLayoutManager: LinearLayoutManager? = null
    private var tagListData: List<RecommendTagModel>? = null


    companion object {
        @JvmStatic
        val DefaultTagUrl = "http://piapro.jp"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.recommand_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Refresh()
        Recommend_Frag_Tag_RecyclerView.setItemViewCacheSize(0)
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
        LoadDefaultPage()
    }

    private fun LoadDefaultPage() {
        DaggerFetchFactory.create()
            .fetcher()
            .visit(DefaultTagUrl)
            .goAsync({ response ->
                SimpleResponseHandler(response, String::class)
                    .Handle({
                        ParseTagListContent(it as String) // Load tags.
                        ParseRecommendListContent(it) // Load all recommend item.
                    }, { code, _ ->
                        HideLoadingIndicator()
                        LoadFailedTips(code, resources.getString(R.string.Error_Page_Load_Failed))
                    })
            }, { e ->
                HideLoadingIndicator()
                activity?.runOnUiThread { LoadFailedTips(-4, e.message ?: "") }
            })
    }

    private fun LoadRecommendList(tagUrl: String) {
        DaggerFetchFactory.create()
            .fetcher()
            .visit(tagUrl)
            .goAsync({ response ->
                SimpleResponseHandler(response, String::class)
                    .Handle({
                        ParseRecommendListContent(it as String)
                    }, { code, _ ->
                        HideLoadingIndicator()
                        LoadFailedTips(code, resources.getString(R.string.Error_Page_Load_Failed))
                    })
            }, { e ->
                HideLoadingIndicator()
                activity?.runOnUiThread { LoadFailedTips(-4, e.message ?: "") }
            })
    }

    private fun OnTagItemSelected(index: Int) {
        val childCount = tagListAdapter!!.itemCount
        for (i in 0 until childCount) {
            val vh = Recommend_Frag_Tag_RecyclerView.findViewHolderForAdapterPosition(i)
            if (vh != null) {
                val tagVH = vh as TagItemAdapter.TagItemVH
                val view = tagVH.itemView as SingleTagView
                if (i == index) {
                    view.SetSelected()
                    ShowLoadingIndicator()
                    LoadRecommendList(DefaultTagUrl + tagListData!![i].URL)
                } else {
                    view.SetDeSelected()
                }
            }
        }
    }

    private fun ParseTagListContent(HTMLString: String) {
        val root = Jsoup.parse(HTMLString)
        val rule = htmlParser.Rules.getJSONObject("RecommendTag").getJSONArray("Steps")
        try {
            tagListData = (htmlParser.Parser.GoSteps(root, rule) as Array<*>).map { it as RecommendTagModel }
            activity?.runOnUiThread {
                tagListLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                Recommend_Frag_Tag_RecyclerView.layoutManager = tagListLayoutManager
                if (tagListAdapter == null) {
                    tagListAdapter = TagItemAdapter(tagListData!!, this::OnTagItemSelected)
                    Recommend_Frag_Tag_RecyclerView.adapter = tagListAdapter
                } else {
                    Recommend_Frag_Tag_RecyclerView.adapter = tagListAdapter
                    tagListAdapter?.items = tagListData!!
                    tagListAdapter?.notifyDataSetChanged()
                }
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

    private fun ParseRecommendListContent(HTMLString: String) {
        val root = Jsoup.parse(HTMLString)
        val rule = htmlParser.Rules.getJSONObject("RecommendList").getJSONArray("Steps")
        try {
            recommendListData = (htmlParser.Parser.GoSteps(root, rule) as Array<*>).map { it as RecommendItemModel }
            activity?.runOnUiThread {

                recommendItemLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                Recommend_Frag_RecyclerView.layoutManager = recommendItemLayoutManager
                if (recommendListAdapter == null) {
                    recommendListAdapter = RecommendItemAdapter(recommendListData!!, context!!)
                    Recommend_Frag_RecyclerView.adapter = recommendListAdapter
                } else {
                    Recommend_Frag_RecyclerView.adapter = recommendListAdapter
                    recommendListAdapter?.items = recommendListData!!
                    recommendListAdapter?.notifyDataSetChanged()
                }
            }
        } catch (e: InvalidStepExecutorException) {
            LoadFailedTips(-1, "InvalidStepExecutorException: ${e.message}")
        } catch (e: ClassNotFoundException) {
            LoadFailedTips(-2, "ClassNotFoundException: ${e.message}")
        } catch (e: Exception) {
            LoadFailedTips(-3, "Exception: ${e.message}")
        } finally {
            HideLoadingIndicator()
        }
    }
}