package com.nemesiss.dev.piaprobox.Fragment.HomePage.TextWork

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nemesiss.dev.HTMLContentParser.InvalidStepExecutorException
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModelText
import com.nemesiss.dev.piaprobox.Activity.Text.TextDetailActivity
import com.nemesiss.dev.piaprobox.Adapter.SubmissionWorkCategory.Text.TextCategoryItemDatabindingAdapter
import com.nemesiss.dev.piaprobox.Fragment.HomePage.BaseSubmissionWorkCategoryFragment
import com.nemesiss.dev.piaprobox.Fragment.HomePage.SubmissionWorkType
import com.nemesiss.dev.piaprobox.Fragment.HomePage.SubmissionWorkUrlBuilder
import com.nemesiss.dev.piaprobox.R
import kotlinx.android.synthetic.main.fragment_header.*
import kotlinx.android.synthetic.main.text_work_fragment.*
import org.jsoup.Jsoup

class TextWorkFragment : BaseSubmissionWorkCategoryFragment() {

    companion object {
        @JvmStatic
        private val MySubmissionType = SubmissionWorkType.TEXT
    }

    private var recommendListData: List<RecommendItemModelText>? = null
    private var recommendListAdapter: TextCategoryItemDatabindingAdapter? = null
    private var recommendListLayoutManager: LinearLayoutManager? = null

    private var CurrentVisitUrl = SubmissionWorkUrlBuilder().type(MySubmissionType).buildString()
    private var CurrentPage = 1


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.text_work_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LoadDefaultSubmissionWorkPage(MySubmissionType)
    }

    override fun LoadBannerImage() {
        BaseMainFragment_Banner_ImageView.setImageResource(R.drawable.text_banner)
    }

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
            ) as Array<*>).map { elem -> elem as RecommendItemModelText }
            activity?.runOnUiThread {
                if (recommendListData is MutableList<RecommendItemModelText>) {
                    val mutableRecommendList = recommendListData as MutableList<RecommendItemModelText>
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

    override fun ParseSubmissionWorkListContent(HTMLString: String, contentType: SubmissionWorkType) {
        val Steps = htmlParser.Rules.getJSONObject("Submission-" + contentType.StepRulePostfix).getJSONArray("Steps")
        try {
            recommendListData = (htmlParser.Parser.GoSteps(
                Jsoup.parse(HTMLString),
                Steps
            ) as Array<*>).map { elem -> elem as RecommendItemModelText }.toMutableList()
            activity?.runOnUiThread {
                recommendListLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                TextCategory_RecyclerView?.layoutManager = recommendListLayoutManager
                if (recommendListAdapter == null) {
                    recommendListAdapter =
                        TextCategoryItemDatabindingAdapter(
                            recommendListData!!,
                            this::OnRecommendItemSelected,
                            TextCategory_RecyclerView
                        ) {
                            LoadMoreItem(CurrentVisitUrl, CurrentPage + 1, contentType)
                        }
                    TextCategory_RecyclerView?.adapter = recommendListAdapter
                } else {
                    TextCategory_RecyclerView?.adapter = recommendListAdapter
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

    override fun ShowLoadMoreIndicatorOnRecyclerView() {
        if (recommendListData is MutableList<RecommendItemModelText>) {
            val mutableRecommendList = recommendListData as MutableList<RecommendItemModelText>
            ShowLoadMoreIndicatorOnRecyclerView(mutableRecommendList, recommendListAdapter!!, false)
        }
    }

    override fun HideLoadMoreIndicatorOnRecyclerView(PendingRefreshAdapterStatus: Boolean) {
        if (recommendListData is MutableList<RecommendItemModelText>) {
            val mutableRecommendList = recommendListData as MutableList<RecommendItemModelText>
            HideLoadMoreIndicatorOnRecyclerView(
                mutableRecommendList,
                recommendListAdapter!!,
                PendingRefreshAdapterStatus
            )
        }
    }

    override fun ShowNothingMoreIndicatorOnRecyclerView() {
        if (recommendListData is MutableList<RecommendItemModelText>) {
            val mutableRecommendList = recommendListData as MutableList<RecommendItemModelText>
            ShowNothingMoreIndicatorOnRecyclerView(
                mutableRecommendList,
                recommendListAdapter!!
            )
        }
    }

    override fun Refresh() {
    }

    override fun OnRecommendItemSelected(index: Int) {
        val item = recommendListData!![index]
        val intent = Intent(context, TextDetailActivity::class.java)
        intent.putExtra(TextDetailActivity.SHOWN_TEXT_INTENT_KEY, item)
        startActivity(intent)
    }
}