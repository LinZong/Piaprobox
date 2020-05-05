package com.nemesiss.dev.piaprobox.Fragment.HomePage.Recommend.Categories

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.SharedElementCallback
import android.support.v7.widget.GridLayoutManager
import android.transition.Transition
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import com.nemesiss.dev.HTMLContentParser.InvalidStepExecutorException
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModelImage
import com.nemesiss.dev.piaprobox.Activity.Image.IllustratorImageProviderActivity
import com.nemesiss.dev.piaprobox.Activity.Image.IllustratorViewActivity2
import com.nemesiss.dev.piaprobox.Adapter.RecommendPage.ImageRecommendItemAdapter
import com.nemesiss.dev.piaprobox.Fragment.HomePage.Recommend.RecommendListType
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.AsyncExecutor
import com.nemesiss.dev.piaprobox.Service.DaggerFactory.DaggerAsyncExecutorFactory
import com.nemesiss.dev.piaprobox.Service.DaggerModules.HTMLParserModules
import com.nemesiss.dev.piaprobox.Util.BaseTransitionCallback
import com.nemesiss.dev.piaprobox.Util.MediaSharedElementCallback
import kotlinx.android.synthetic.main.recommend_category_layout.*
import kotlinx.android.synthetic.main.single_recommend_image_item.view.*
import org.jsoup.Jsoup
import javax.inject.Inject

class RecommendImageCategoryFragment : BaseRecommendCategoryFragment() {

    @Inject
    lateinit var asyncExecutor: AsyncExecutor

    private var recommendListAdapter: ImageRecommendItemAdapter? = null
    private var recommendItemLayoutManager: GridLayoutManager? = null
    private var recommendListData: List<RecommendItemModelImage>? = null

    override var CurrentCategoryFragmentType: RecommendListType = RecommendListType.IMAGE


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DaggerAsyncExecutorFactory
            .builder()
            .hTMLParserModules(HTMLParserModules(context!!))
            .build()
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.recommend_category_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LoadDefaultPage(CurrentCategoryFragmentType, true)
    }

    override fun Refresh() {
        LoadRecommendList(CurrentLoadTagPageURL, CurrentCategoryFragmentType)
    }

    override fun OnRecommendItemSelected(index: Int) {

    }
    fun onActivityReenter(resultCode: Int, intent: Intent?) {
        when (resultCode) {
            // 处理来自IllustratorViewActivity2的Re-enter.
            IllustratorViewActivity2.REENTER_RESULT_CODE -> {
                val position = intent?.getIntExtra("CURRENT_INDEX", -1)!!
                if (position != -1) {
                    ScrollToPositionIfNotFullyVisible(position, false)
                }
                val sharedElementCallback = MediaSharedElementCallback()
                activity?.setExitSharedElementCallback(sharedElementCallback)
                activity?.window?.sharedElementExitTransition?.addListener(object : BaseTransitionCallback() {
                    override fun onTransitionEnd(p0: Transition?) {
                        removeCallback()
                    }

                    override fun onTransitionCancel(p0: Transition?) {
                        removeCallback()
                    }
                    private fun removeCallback() {
                        if (activity != null) {
                            activity!!.window.sharedElementExitTransition.removeListener(this)
                            activity!!.setExitSharedElementCallback(object : SharedElementCallback() {})
                        }
                    }
                })
                activity?.supportPostponeEnterTransition()
                Recommend_Frag_Common_RecyclerView.viewTreeObserver.addOnPreDrawListener(object :
                    ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        val vh = Recommend_Frag_Common_RecyclerView.findViewHolderForAdapterPosition(position)
                        if (vh != null && vh is ImageRecommendItemAdapter.ImageRecommendItemViewHolder) {
                            sharedElementCallback.setSharedElementViews(
                                arrayOf(resources.getString(R.string.ImageViewTransitionName)),
                                arrayOf(vh.itemView.SingleImageWorkItemCard_WorkThumb)
                            )
                            Recommend_Frag_Common_RecyclerView.viewTreeObserver.removeOnPreDrawListener(this)
                            activity?.supportStartPostponedEnterTransition()
                            // 可以清除CallbackListener.
                        }
                        return true
                    }
                })
            }
        }
    }

    fun ScrollToPositionIfNotFullyVisible(position: Int, smooth: Boolean): Boolean {
        val view = recommendItemLayoutManager?.findViewByPosition(position)
        val notVisible = view == null || recommendItemLayoutManager!!.isViewPartiallyVisible(view, false, true)
        if (notVisible) {
            if (smooth) {
                Recommend_Frag_Common_RecyclerView.smoothScrollToPosition(position)
            } else {
                recommendItemLayoutManager?.scrollToPosition(position)
            }
        }
        return notVisible
    }


    fun OnRecommendItemSelectedWithSharedImageView(index: Int, SharedImageView: ImageView) {

        val notVisible = ScrollToPositionIfNotFullyVisible(index, true)

        asyncExecutor.SendTaskMainThreadDelay(
            Runnable {
                val intent = Intent(context, IllustratorViewActivity2::class.java)
                intent.putExtra(IllustratorViewActivity2.CLICKED_ITEM_INDEX, index)
                IllustratorImageProviderActivity.SetItemList(recommendListData!!)
                IllustratorImageProviderActivity.SetPreShownDrawable(SharedImageView.drawable)
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity!!,
                    SharedImageView,
                    resources.getString(R.string.ImageViewTransitionName)
                )
                startActivity(intent, options.toBundle())
            }, if (notVisible) 100 else 0
        )
    }

    override fun ParseRecommendListContent(HTMLString: String, contentType: RecommendListType) {
        val root = Jsoup.parse(HTMLString)
        val rule = htmlParser.Rules.getJSONObject("RecommendList-" + contentType.Name).getJSONArray("Steps")
        try {
            recommendListData =
                (htmlParser.Parser.GoSteps(root, rule) as Array<*>).map { it as RecommendItemModelImage }
            activity?.runOnUiThread {
                recommendItemLayoutManager = GridLayoutManager(context, 2)
                Recommend_Frag_Common_RecyclerView.layoutManager = recommendItemLayoutManager
                if (recommendListAdapter == null) {
                    recommendListAdapter =
                        ImageRecommendItemAdapter(
                            recommendListData!!,
                            context!!,
                            this::OnRecommendItemSelectedWithSharedImageView
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
}