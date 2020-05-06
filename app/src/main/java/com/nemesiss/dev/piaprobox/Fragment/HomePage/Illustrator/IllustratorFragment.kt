package com.nemesiss.dev.piaprobox.Fragment.HomePage.Illustrator

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nemesiss.dev.HTMLContentParser.InvalidStepExecutorException
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModelImage
import com.nemesiss.dev.piaprobox.Adapter.SubmissionWorkCategory.Music.IllustrationCategoryItemDatabindingAdapter
import com.nemesiss.dev.piaprobox.Fragment.HomePage.BaseSubmissionWorkCategoryFragment
import com.nemesiss.dev.piaprobox.Fragment.HomePage.SubmissionWorkType
import com.nemesiss.dev.piaprobox.Fragment.HomePage.SubmissionWorkUrlBuilder
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Util.isLoadMoreIndicator
import com.nemesiss.dev.piaprobox.Util.isNoMoreIndicator
import com.nemesiss.dev.piaprobox.View.Common.GridLayoutManagerWithBottomIndicator
import kotlinx.android.synthetic.main.fragment_header.*
import kotlinx.android.synthetic.main.illustrator_main_fragment.*
import org.jsoup.Jsoup

class IllustratorFragment : BaseSubmissionWorkCategoryFragment() {

    companion object {
        @JvmStatic
        private val MySubmissionType = SubmissionWorkType.ILLUSTRATION
    }

    private var recommendListData: List<RecommendItemModelImage>? = null
    private var recommendListAdapter: IllustrationCategoryItemDatabindingAdapter? = null
    private var recommendListLayoutManager: GridLayoutManager? = null

    private var CurrentVisitUrl = SubmissionWorkUrlBuilder().type(MySubmissionType).buildString()
    private var CurrentPage = 1

    override fun AppendSubmissionWorkListContent(
        HTMLString: String,
        submissionWorkType: SubmissionWorkType,
        ReachPageLimit: Boolean
    ) {
        if (ReachPageLimit) {
            ShowNothingMoreIndicatorOnRecyclerView()
            return
        }

        var indicatorCleared = false
        val Steps = htmlParser
            .Rules
            .getJSONObject("Submission-" + submissionWorkType.StepRulePostfix)
            .getJSONArray("Steps")
        try {
            val appendixItem = (htmlParser.Parser.GoSteps(
                Jsoup.parse(HTMLString),
                Steps
            ) as Array<*>).map { elem -> elem as RecommendItemModelImage }
            activity?.runOnUiThread {
                if (recommendListData is MutableList<RecommendItemModelImage>) {
                    val mutableRecommendList = recommendListData as MutableList<RecommendItemModelImage>
                    val listOldLength = mutableRecommendList.size
                    mutableRecommendList.addAll(appendixItem)
                    // 取消掉finally中对失败情形进行集中处理的逻辑
                    indicatorCleared = true
                    // 确实可以翻到下一页了，变量自增.
                    CurrentPage++
                    // 因为接下来马上就要notifyDataSetChanged了。所以不需要在HideLoadMoreIndicator中再刷新一个DataSet.
                    HideLoadMoreIndicatorOnRecyclerView(true)
                    // 使用itemRangeChanged方法，减少闪屏
                    recommendListAdapter?.notifyItemRangeChanged(listOldLength, 1 + appendixItem.size)
                } else {
                    LoadFailedTips(
                        -5,
                        "Current `recommendListData` is not a mutable list, cannot append elements in it."
                    )
                }
            }
        } catch (e: InvalidStepExecutorException) {
            LoadFailedTips(-1, "InvalidStepExecutorException: ${e.message}")
        } catch (e: ClassNotFoundException) {
            LoadFailedTips(-2, "ClassNotFoundException: ${e.message}")
        } catch (e: Exception) {
            LoadFailedTips(-3, "Exception: ${e.message}")
        } finally {
            if (!indicatorCleared) {
                HideLoadMoreIndicatorOnRecyclerView(false)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.illustrator_main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LoadDefaultSubmissionWorkPage(MySubmissionType)
    }

    override fun LoadBannerImage() {
        BaseMainFragment_Banner_ImageView.setImageResource(R.drawable.illu_banner)
    }

    override fun ShowLoadMoreIndicatorOnRecyclerView() {
        if (recommendListData is MutableList<RecommendItemModelImage>) {
            val mutableRecommendList = recommendListData as MutableList<RecommendItemModelImage>
            ShowLoadMoreIndicatorOnRecyclerView(mutableRecommendList, recommendListAdapter!!, false)
        }
    }

    override fun HideLoadMoreIndicatorOnRecyclerView(PendingRefreshAdapterStatus: Boolean) {
        if (recommendListData is MutableList<RecommendItemModelImage>) {
            val mutableRecommendList = recommendListData as MutableList<RecommendItemModelImage>
            HideLoadMoreIndicatorOnRecyclerView(
                mutableRecommendList,
                recommendListAdapter!!,
                PendingRefreshAdapterStatus
            )
        }
    }

    override fun ShowNothingMoreIndicatorOnRecyclerView() {
        if (recommendListData is MutableList<RecommendItemModelImage>) {
            ShowNothingMoreIndicatorOnRecyclerView(
                recommendListData as MutableList<RecommendItemModelImage>,
                recommendListAdapter!!
            )
        }
    }

    override fun Refresh() {

    }

    override fun OnRecommendItemSelected(index: Int) {
    }

    override fun ParseSubmissionWorkListContent(HTMLString: String, contentType: SubmissionWorkType) {
        val Steps = htmlParser.Rules.getJSONObject("Submission-" + contentType.StepRulePostfix).getJSONArray("Steps")
        try {
            recommendListData = (htmlParser.Parser.GoSteps(
                Jsoup.parse(HTMLString),
                Steps
            ) as Array<*>).map { elem -> elem as RecommendItemModelImage }.toMutableList()
            activity?.runOnUiThread {
                recommendListLayoutManager =
                    GridLayoutManagerWithBottomIndicator(context, 2) spanSizeJudge@{ position ->
                        if(recommendListAdapter!!.getItemViewType(position) == 0) 1 else 2
                    }
                IllustratorCategory_RecyclerView.layoutManager = recommendListLayoutManager
                if (recommendListAdapter == null) {
                    recommendListAdapter =
                        IllustrationCategoryItemDatabindingAdapter(
                            recommendListData!!,
                            this::OnRecommendItemSelected,
                            IllustratorCategory_RecyclerView
                        ) {
                            LoadMoreItem(CurrentVisitUrl, CurrentPage + 1, contentType)
                        }
                    IllustratorCategory_RecyclerView.adapter = recommendListAdapter
                } else {
                    IllustratorCategory_RecyclerView.adapter = recommendListAdapter
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