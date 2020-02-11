package com.nemesiss.dev.piaprobox.Fragment.Recommend.Categories

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.SharedElementCallback
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable
import com.bumptech.glide.request.target.SquaringDrawable
import com.nemesiss.dev.HTMLContentParser.InvalidStepExecutorException
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModel
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModelImage
import com.nemesiss.dev.HTMLContentParser.Model.RecommendTagModel
import com.nemesiss.dev.piaprobox.Activity.Image.IllustratorViewActivity
import com.nemesiss.dev.piaprobox.Activity.Image.IllustratorViewActivity2
import com.nemesiss.dev.piaprobox.Activity.Music.MusicControlActivity
import com.nemesiss.dev.piaprobox.Activity.Music.MusicPlayerActivity
import com.nemesiss.dev.piaprobox.Adapter.Common.TagItemAdapter
import com.nemesiss.dev.piaprobox.Adapter.RecommendPage.ImageRecommendItemAdapter
import com.nemesiss.dev.piaprobox.Adapter.RecommendPage.MusicRecommendItemAdapter
import com.nemesiss.dev.piaprobox.Application.PiaproboxApplication
import com.nemesiss.dev.piaprobox.Fragment.Recommend.MainRecommendFragment
import com.nemesiss.dev.piaprobox.Fragment.Recommend.RecommendListType
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.HTMLParser
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.DaggerFetchFactory
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.SimpleResponseHandler
import com.nemesiss.dev.piaprobox.View.Common.SingleTagView
import kotlinx.android.synthetic.main.recommend_category_layout.*
import kotlinx.android.synthetic.main.single_recommend_image_item.view.*
import org.jsoup.Jsoup

class RecommendImageCategoryFragment : BaseRecommendCategoryFragment()
{
    private var recommendListAdapter: ImageRecommendItemAdapter? = null
    private var recommendItemLayoutManager: GridLayoutManager? = null
    private var recommendListData: List<RecommendItemModelImage>? = null

    override var CurrentCategoryFragmentType: RecommendListType = RecommendListType.IMAGE

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.recommend_category_layout, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LoadDefaultPage(CurrentCategoryFragmentType,true)
    }

    override fun Refresh() {
        LoadRecommendList(CurrentLoadTagPageURL, CurrentCategoryFragmentType)
    }

    override fun OnRecommendItemSelected(index: Int) {

    }

    fun ScrollToPositionAndReturnView(position : Int) : ImageView? {
        Log.d("RecommendImage","ScrollToPositionAndReturnView  ${position}")
        val view = recommendItemLayoutManager?.findViewByPosition(position)
        if(view == null || recommendItemLayoutManager!!.isViewPartiallyVisible(view, false, true))
        {
            recommendItemLayoutManager?.scrollToPosition(position)
        }
        return (Recommend_Frag_Common_RecyclerView.findViewHolderForAdapterPosition(position) as? ImageRecommendItemAdapter.ImageRecommendItemViewHolder)?.itemView?.SingleImageWorkItemCard_WorkThumb
    }

    fun OnRecommendItemSelectedWithSharedImageView(index: Int, SharedImageView : ImageView) {

        val intent = Intent(context, IllustratorViewActivity2::class.java)
        intent.putExtra(IllustratorViewActivity2.CLICKED_ITEM_INDEX, index)

        IllustratorViewActivity2.SetItemList(recommendListData!!)
        IllustratorViewActivity2.SetPreShownDrawable(SharedImageView.drawable)
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity!!, SharedImageView, resources.getString(R.string.ImageViewTransitionName))
        startActivity(intent, options.toBundle())
    }

    override fun ParseRecommendListContent(HTMLString: String, contentType: RecommendListType) {
        val root = Jsoup.parse(HTMLString)
        val rule = htmlParser.Rules.getJSONObject("RecommendList-" + contentType.Name).getJSONArray("Steps")
        try {
            recommendListData = (htmlParser.Parser.GoSteps(root, rule) as Array<*>).map { it as RecommendItemModelImage }
            activity?.runOnUiThread {
                recommendItemLayoutManager = GridLayoutManager(context,2)
                Recommend_Frag_Common_RecyclerView.layoutManager = recommendItemLayoutManager
                if (recommendListAdapter == null) {
                    recommendListAdapter =
                        ImageRecommendItemAdapter(recommendListData!!, context!!, this::OnRecommendItemSelectedWithSharedImageView)
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