package com.nemesiss.dev.piaprobox.Fragment.HomePage.Music

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nemesiss.dev.HTMLContentParser.InvalidStepExecutorException
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModel
import com.nemesiss.dev.piaprobox.Adapter.RecommendPage.ImageRecommendItemAdapter
import com.nemesiss.dev.piaprobox.Adapter.RecommendPage.MusicRecommendItemAdapter
import com.nemesiss.dev.piaprobox.Adapter.RecommendPage.MusicRecommendItemDatabindingAdapter
import com.nemesiss.dev.piaprobox.Fragment.HomePage.BaseSubmissionWorkCategoryFragment
import com.nemesiss.dev.piaprobox.Fragment.HomePage.SubmissionWorkType
import com.nemesiss.dev.piaprobox.R
import kotlinx.android.synthetic.main.fragment_header.*
import kotlinx.android.synthetic.main.recommend_category_layout.*
import org.jsoup.Jsoup

class MusicFragment : BaseSubmissionWorkCategoryFragment() {

    private var recommendListAdapter: MusicRecommendItemDatabindingAdapter? = null
    private var recommendItemLayoutManager: LinearLayoutManager? = null
    private var recommendListData: List<RecommendItemModel>? = null

    override fun Refresh() {
    }

    override fun OnRecommendItemSelected(index: Int) {
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.music_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LoadDefaultSubmissionWorkPage(SubmissionWorkType.MUSIC)
    }

    override fun LoadBannerImage() {
        BaseMainFragment_Banner_ImageView.setImageResource(R.drawable.music_banner)
    }

    override fun ParseSubmissionWorkListContent(HTMLString: String, contentType: SubmissionWorkType) {
        val Steps = htmlParser.Rules.getJSONObject("Submission-" + contentType.StepRulePostfix).getJSONArray("Steps")
        try {
            recommendListData = (htmlParser.Parser.GoSteps(
                Jsoup.parse(HTMLString),
                Steps
            ) as Array<*>).map { elem -> elem as RecommendItemModel }
            activity?.runOnUiThread {
                recommendItemLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                Recommend_Frag_Common_RecyclerView.layoutManager = recommendItemLayoutManager
                if (recommendListAdapter == null) {
                    recommendListAdapter =
                        MusicRecommendItemDatabindingAdapter(
                            recommendListData!!,
                            context!!,
                            this::OnRecommendItemSelected
                        )
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

    private fun ShowLoadMoreIndicatorOnRecyclerView() {

    }

    private fun HideLoadMoreIndicatorOnRecyclerView() {

    }

    private fun ShowNothingMoreIndicatorOnRecyclerView() {

    }

    override fun AppendSubmissionWorkListContent(
        HTMLString: String,
        submissionWorkType: SubmissionWorkType,
        ReachPageLimit: Boolean
    ) {

    }
}