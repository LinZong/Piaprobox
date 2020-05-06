package com.nemesiss.dev.piaprobox.Adapter.SubmissionWorkCategory.Music

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModel
import com.nemesiss.dev.piaprobox.Adapter.Common.InfinityLoadAdapter
import com.nemesiss.dev.piaprobox.Misc.RecyclerViewInnerIndicator
import com.nemesiss.dev.piaprobox.Util.detectWhichViewHolderToCreate
import com.nemesiss.dev.piaprobox.View.Common.RecyclerViewLoadingIndicatorViewHolder
import com.nemesiss.dev.piaprobox.View.Common.RecyclerViewNothingMoreIndicatorViewHolder
import com.nemesiss.dev.piaprobox.databinding.SingleRecommendItemBinding

class MusicCategoryItemDatabindingAdapter(
    var items: List<RecommendItemModel>,
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
        detectWhichViewHolderToCreate(RecommendItemDatabindingVH::class)(viewGroup, viewType)

    override fun getItemCount(): Int = items.size


    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, position: Int) {
        if (vh is RecommendItemDatabindingVH) {
            vh.binding.model = items[position]
            vh.binding.root.setOnClickListener { itemSelected(position) }
        }
    }
}