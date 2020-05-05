package com.nemesiss.dev.piaprobox.Fragment.HomePage.Music

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nemesiss.dev.HTMLContentParser.InvalidStepExecutorException
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModel
import com.nemesiss.dev.piaprobox.Adapter.SubmissionWorkCategory.Music.MusicCategoryItemDatabindingAdapter
import com.nemesiss.dev.piaprobox.Fragment.HomePage.BaseSubmissionWorkCategoryFragment
import com.nemesiss.dev.piaprobox.Fragment.HomePage.SubmissionWorkType
import com.nemesiss.dev.piaprobox.Fragment.HomePage.SubmissionWorkUrlBuilder
import com.nemesiss.dev.piaprobox.Misc.RecyclerViewInnerIndicator
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Util.removeIndicator
import kotlinx.android.synthetic.main.fragment_header.*
import kotlinx.android.synthetic.main.music_fragment.*
import org.jsoup.Jsoup

class MusicFragment : BaseSubmissionWorkCategoryFragment() {

    private var recommendListAdapter: MusicCategoryItemDatabindingAdapter? = null
    private var recommendItemLayoutManager: LinearLayoutManager? = null
    private var recommendListData: List<RecommendItemModel>? = null

    private var CurrentVisitUrl = SubmissionWorkUrlBuilder().type(SubmissionWorkType.MUSIC).buildString()
    private var CurrentPage = 1

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
            ) as Array<*>).map { elem -> elem as RecommendItemModel }.toMutableList()
            activity?.runOnUiThread {
                recommendItemLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                MusicCategory_RecyclerView.layoutManager = recommendItemLayoutManager
                if (recommendListAdapter == null) {
                    recommendListAdapter =
                        MusicCategoryItemDatabindingAdapter(
                            recommendListData!!,
                            context!!,
                            this::OnRecommendItemSelected,
                            MusicCategory_RecyclerView
                        ) {
                            LoadMoreItem(CurrentVisitUrl, CurrentPage + 1, SubmissionWorkType.MUSIC)
                        }
                    MusicCategory_RecyclerView.adapter = recommendListAdapter
                } else {
                    MusicCategory_RecyclerView.adapter = recommendListAdapter
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


    override fun AppendSubmissionWorkListContent(
        HTMLString: String,
        submissionWorkType: SubmissionWorkType,
        ReachPageLimit: Boolean
    ) {
        if(ReachPageLimit) {
            ShowNothingMoreIndicatorOnRecyclerView()
            return
        }

        var indicatorCleared = false
        val Steps = htmlParser
            .Rules
            .getJSONObject("Submission-" + SubmissionWorkType.MUSIC.StepRulePostfix)
            .getJSONArray("Steps")
        try {
            val appendixItem = (htmlParser.Parser.GoSteps(
                Jsoup.parse(HTMLString),
                Steps
            ) as Array<*>).map { elem -> elem as RecommendItemModel }
            activity?.runOnUiThread {
                if (recommendListData is MutableList<RecommendItemModel>) {
                    val mutableRecommendList = recommendListData as MutableList<RecommendItemModel>
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

    override fun ShowLoadMoreIndicatorOnRecyclerView() {
        if (recommendListData is MutableList<RecommendItemModel>) {
            val mutableRecommendList = recommendListData as MutableList<RecommendItemModel>
            mutableRecommendList.add(RecommendItemModel().apply {
                URL = RecyclerViewInnerIndicator.RECYCLER_VIEW_LOAD_MORE_INDICATOR.TAG
            })
            recommendListAdapter?.notifyItemInserted(mutableRecommendList.size - 1)
        }
    }

    override fun HideLoadMoreIndicatorOnRecyclerView(PendingRefreshAdapterStatus: Boolean) {
        if (recommendListData is MutableList<RecommendItemModel>) {
            val mutableRecommendList = recommendListData as MutableList<RecommendItemModel>
            val removedIndex = mutableRecommendList.removeIndicator()
            if (!PendingRefreshAdapterStatus && removedIndex > 0) {
                activity?.runOnUiThread { recommendListAdapter?.notifyItemRemoved(removedIndex) }
            }
        }
        // 告知它已经完成加载了.
        recommendListAdapter?.loaded()
    }

    override fun ShowNothingMoreIndicatorOnRecyclerView() {
        if (recommendListData is MutableList<RecommendItemModel>) {
            val mutableRecommendList = recommendListData as MutableList<RecommendItemModel>
            mutableRecommendList.add(
                RecommendItemModel().apply {
                    URL = RecyclerViewInnerIndicator.RECYCLER_VIEW_NOTHING_MORE_INDICATOR.TAG
                }
            )
            recommendListAdapter?.notifyItemInserted(mutableRecommendList.size - 1)
            recommendListAdapter?.loaded()
            recommendListAdapter?.disable()
            // 保持 "再怎么找也没有啦 >_< 显示在RecyclerView的最下面"
//            asyncExecutor.SendTaskMainThreadDelay(Runnable {
//                val removedIndex = mutableRecommendList.removeIndicator()
//                if (removedIndex > 0) {
//                    recommendListAdapter?.notifyItemRemoved(removedIndex)
//                }
//                // 告知它已经完成加载了.
//                recommendListAdapter?.loaded()
//            }, 2000L)
        }
    }
}