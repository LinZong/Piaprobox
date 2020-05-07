package com.nemesiss.dev.piaprobox.Fragment.HomePage.Recommend.Categories

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nemesiss.dev.HTMLContentParser.InvalidStepExecutorException
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModelText
import com.nemesiss.dev.piaprobox.Activity.Text.TextDetailActivity
import com.nemesiss.dev.piaprobox.Adapter.RecommendPage.TextRecommendItemDatabindingAdapter
import com.nemesiss.dev.piaprobox.Fragment.HomePage.Recommend.RecommendListType
import com.nemesiss.dev.piaprobox.R
import kotlinx.android.synthetic.main.recommend_category_layout.*
import org.jsoup.Jsoup

class RecommendTextCategoryFragment : BaseRecommendCategoryFragment() {

    private var recommendListAdapter: TextRecommendItemDatabindingAdapter? = null
    private var recommendItemLayoutManager: LinearLayoutManager? = null
    private var recommendListData: List<RecommendItemModelText>? = null

    override var CurrentCategoryFragmentType = RecommendListType.TEXT

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.recommend_category_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LoadDefaultRecommendPage(RecommendListType.TEXT)
    }

    override fun Refresh() {
        LoadRecommendList(CurrentLoadTagPageURL, CurrentCategoryFragmentType)
    }

    override fun OnRecommendItemSelected(index: Int) {
        val item = recommendListData!![index]
        val intent = Intent(context, TextDetailActivity::class.java)
        intent.putExtra(TextDetailActivity.SHOWN_TEXT_INTENT_KEY, item)
        startActivity(intent)
    }

    override fun ParseRecommendListContent(HTMLString: String, contentType: RecommendListType) {
        val root = Jsoup.parse(HTMLString)
        val rule = htmlParser.Rules.getJSONObject("RecommendList-" + contentType.Name).getJSONArray("Steps")
        try {
            recommendListData = (htmlParser.Parser.GoSteps(root, rule) as Array<*>).map { it as RecommendItemModelText }

            activity?.runOnUiThread {
                recommendItemLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                Recommend_Frag_Common_RecyclerView?.layoutManager = recommendItemLayoutManager
                if (recommendListAdapter == null) {
                    recommendListAdapter =
                        TextRecommendItemDatabindingAdapter(
                            recommendListData!!,
                            context!!,
                            this::OnRecommendItemSelected
                        )
                    Recommend_Frag_Common_RecyclerView?.adapter = recommendListAdapter
                } else {
                    Recommend_Frag_Common_RecyclerView?.adapter = recommendListAdapter
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