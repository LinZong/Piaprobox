package com.nemesiss.dev.piaprobox.Fragment.HomePage.Recommend.Categories

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.nemesiss.dev.HTMLContentParser.InvalidStepExecutorException
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModelImage
import com.nemesiss.dev.piaprobox.Activity.Image.IllustratorImageProviderActivity
import com.nemesiss.dev.piaprobox.Activity.Image.IllustratorViewActivity2
import com.nemesiss.dev.piaprobox.Adapter.RecommendPage.ImageRecommendItemDatabindingAdapter
import com.nemesiss.dev.piaprobox.Fragment.HomePage.Recommend.RecommendListType
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.DaggerFactory.DaggerAsyncExecutorFactory
import com.nemesiss.dev.piaprobox.Service.DaggerModules.HTMLParserModules
import com.nemesiss.dev.piaprobox.View.Common.handleSharedElementReenter
import kotlinx.android.synthetic.main.recommend_category_layout.*
import org.jsoup.Jsoup

class RecommendImageCategoryFragment : BaseRecommendCategoryFragment() {

    private var recommendListAdapter: ImageRecommendItemDatabindingAdapter? = null
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
        LoadDefaultRecommendPage(CurrentCategoryFragmentType, true)
    }

    override fun Refresh() {
        LoadRecommendList(CurrentLoadTagPageURL, CurrentCategoryFragmentType)
    }

    override fun OnRecommendItemSelected(index: Int) {

    }

    fun onActivityReenter(resultCode: Int, intent: Intent?) {
        handleSharedElementReenter(Recommend_Frag_Common_RecyclerView) calcScrollPos@{
            val position = intent?.getIntExtra("CURRENT_INDEX", -1)!!
            if (position != -1) {
                ScrollToPositionIfNotFullyVisible(position, false)
            }
            return@calcScrollPos position
        }(resultCode, intent)
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
                        ImageRecommendItemDatabindingAdapter(
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