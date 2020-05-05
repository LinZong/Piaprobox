package com.nemesiss.dev.piaprobox.Adapter.SubmissionWorkCategory.Music

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModel
import com.nemesiss.dev.piaprobox.Adapter.Common.InfinityLoadAdapter
import com.nemesiss.dev.piaprobox.Adapter.RecommendPage.MusicRecommendItemDatabindingAdapter
import com.nemesiss.dev.piaprobox.Misc.RecyclerViewInnerIndicator
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Util.fixThumb
import com.nemesiss.dev.piaprobox.View.Common.RecyclerViewLoadingIndicatorViewHolder
import com.nemesiss.dev.piaprobox.View.Common.RecyclerViewNothingMoreIndicatorViewHolder
import com.nemesiss.dev.piaprobox.databinding.SingleRecommendItemBinding

class MusicCategoryItemDatabindingAdapter(
    var items: List<RecommendItemModel>,
    val context: Context,
    private inline val itemSelected: (Int) -> Unit,
    attachedRecyclerView: RecyclerView,
    ShouldLoadMoreItem: () -> Unit
) : InfinityLoadAdapter<RecyclerView.ViewHolder>(
    attachedRecyclerView, ShouldLoadMoreItem
) {
    class RecommendItemDatabindingVH private constructor(val binding: SingleRecommendItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            @JvmStatic
            fun create(viewGroup: ViewGroup): RecommendItemDatabindingVH {
                return RecommendItemDatabindingVH(
                    SingleRecommendItemBinding.inflate(
                        LayoutInflater.from(viewGroup.context),
                        viewGroup,
                        false
                    )
                )
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return RecyclerViewInnerIndicator.values().find { indicator -> indicator.TAG == items[position].URL }?.FLAG ?: 0
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            RecyclerViewInnerIndicator.RECYCLER_VIEW_LOAD_MORE_INDICATOR.FLAG
            -> RecyclerViewLoadingIndicatorViewHolder.create(
                viewGroup
            )
            RecyclerViewInnerIndicator.RECYCLER_VIEW_NOTHING_MORE_INDICATOR.FLAG
            -> RecyclerViewNothingMoreIndicatorViewHolder.create(
                viewGroup
            )
            else -> RecommendItemDatabindingVH.create(
                viewGroup
            )
        }

    override fun getItemCount(): Int = items.size


    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, position: Int) {
        if (vh is RecommendItemDatabindingVH) {
            vh.binding.model = items[position]
            vh.binding.root.setOnClickListener { itemSelected(position) }
        }
    }
}