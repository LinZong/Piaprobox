package com.nemesiss.dev.piaprobox.Fragment.HomePage.Music

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.nemesiss.dev.HTMLContentParser.InvalidStepExecutorException
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModel
import com.nemesiss.dev.piaprobox.Activity.Music.MusicControlActivity
import com.nemesiss.dev.piaprobox.Activity.Music.MusicPlayerActivity
import com.nemesiss.dev.piaprobox.Adapter.SubmissionWorkCategory.Music.MusicCategoryItemDatabindingAdapter
import com.nemesiss.dev.piaprobox.Fragment.HomePage.BaseSubmissionWorkCategoryFragment
import com.nemesiss.dev.piaprobox.Fragment.HomePage.SubmissionWorkType
import com.nemesiss.dev.piaprobox.Fragment.HomePage.SubmissionWorkUrlBuilder
import com.nemesiss.dev.piaprobox.Model.Resources.RecyclerViewInnerIndicator
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.HTMLParser
import com.nemesiss.dev.piaprobox.View.Common.isLoadMoreIndicator
import com.nemesiss.dev.piaprobox.View.Common.isNoMoreIndicator
import kotlinx.android.synthetic.main.fragment_header.*
import kotlinx.android.synthetic.main.category_fragment.*
import org.jsoup.Jsoup

class MusicFragment : BaseSubmissionWorkCategoryFragment() {

    override val MySubmissionType: SubmissionWorkType = SubmissionWorkType.MUSIC

    private var recommendListAdapter: MusicCategoryItemDatabindingAdapter? = null
    private var recommendItemLayoutManager: LinearLayoutManager? = null
    private var recommendListData: List<RecommendItemModel>? = null

    private var CurrentVisitUrl = SubmissionWorkUrlBuilder().type(MySubmissionType).buildString()
    private var CurrentPage = 1


    override fun OnRecommendItemSelected(index: Int) {
        val item = recommendListData!![index]
        val intent = Intent(context, MusicControlActivity::class.java)
        intent.putExtra(MusicPlayerActivity.CLICK_ITEM_INDEX, index)
        intent.putExtra(MusicPlayerActivity.MUSIC_CONTENT_URL, HTMLParser.WrapDomain(item.URL))
        MusicPlayerActivity.PLAY_LISTS =
            recommendListData!!.filter { model -> !(model.isLoadMoreIndicator() || model.isNoMoreIndicator()) }
        startActivity(intent)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LoadDefaultSubmissionWorkPage(MySubmissionType)
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
                Category_RecyclerView?.layoutManager = recommendItemLayoutManager
                if (recommendListAdapter == null) {
                    recommendListAdapter =
                        MusicCategoryItemDatabindingAdapter(
                            recommendListData!!,
                            this::OnRecommendItemSelected,
                            Category_RecyclerView
                        ) {
                            LoadMoreItem(CurrentVisitUrl, CurrentPage + 1, contentType)
                        }
                    Category_RecyclerView?.adapter = recommendListAdapter
                } else {
                    Category_RecyclerView?.adapter = recommendListAdapter
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
            ShowLoadMoreIndicatorOnRecyclerView(mutableRecommendList, recommendListAdapter!!, false)
        }
    }

    override fun HideLoadMoreIndicatorOnRecyclerView(PendingRefreshAdapterStatus: Boolean) {
        if (recommendListData is MutableList<RecommendItemModel>) {
            val mutableRecommendList = recommendListData as MutableList<RecommendItemModel>
            HideLoadMoreIndicatorOnRecyclerView(
                mutableRecommendList,
                recommendListAdapter!!,
                PendingRefreshAdapterStatus
            )
        }
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
            // 保持 "这里什么也没有啦  >_< 显示在RecyclerView的最下面"
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