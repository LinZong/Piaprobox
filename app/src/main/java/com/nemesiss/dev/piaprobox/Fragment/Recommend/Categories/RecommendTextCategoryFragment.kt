package com.nemesiss.dev.piaprobox.Fragment.Recommend.Categories

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nemesiss.dev.HTMLContentParser.InvalidStepExecutorException
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModel
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModelText
import com.nemesiss.dev.HTMLContentParser.Model.RecommendTagModel
import com.nemesiss.dev.piaprobox.Adapter.Common.TagItemAdapter
import com.nemesiss.dev.piaprobox.Adapter.RecommendPage.MusicRecommendItemAdapter
import com.nemesiss.dev.piaprobox.Adapter.RecommendPage.TextRecommendItemAdapter
import com.nemesiss.dev.piaprobox.Application.PiaproboxApplication
import com.nemesiss.dev.piaprobox.Fragment.Recommend.MainRecommendFragment
import com.nemesiss.dev.piaprobox.Fragment.Recommend.RecommendListType
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.HTMLParser
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.DaggerFetchFactory
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.SimpleResponseHandler
import com.nemesiss.dev.piaprobox.View.Common.SingleTagView
import kotlinx.android.synthetic.main.recommend_category_layout.*
import org.jsoup.Jsoup

class RecommendTextCategoryFragment : BaseRecommendCategoryFragment()
{

    private var recommendListAdapter: TextRecommendItemAdapter? = null
    private var recommendItemLayoutManager: LinearLayoutManager? = null
    private var recommendListData: List<RecommendItemModelText>? = null

    override var CurrentCategoryFragmentType = RecommendListType.TEXT

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.recommend_category_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LoadDefaultPage(RecommendListType.TEXT)
    }
    override fun Refresh() {
        LoadRecommendList(CurrentLoadTagPageURL, CurrentCategoryFragmentType)
    }

    override fun OnRecommendItemSelected(index: Int) {

    }


    override fun ParseRecommendListContent(HTMLString: String, contentType: RecommendListType) {
        val root = Jsoup.parse(HTMLString)
        val rule = htmlParser.Rules.getJSONObject("RecommendList-" + contentType.Name).getJSONArray("Steps")
        try {
            recommendListData = (htmlParser.Parser.GoSteps(root, rule) as Array<*>).map { it as RecommendItemModelText }

            activity?.runOnUiThread {
                recommendItemLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                Recommend_Frag_Common_RecyclerView.layoutManager = recommendItemLayoutManager
                if (recommendListAdapter == null) {
                    recommendListAdapter =
                        TextRecommendItemAdapter(recommendListData!!, context!!, this::OnRecommendItemSelected)
                    Recommend_Frag_Common_RecyclerView.adapter = recommendListAdapter
                } else {
                    Recommend_Frag_Common_RecyclerView.adapter = recommendListAdapter
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