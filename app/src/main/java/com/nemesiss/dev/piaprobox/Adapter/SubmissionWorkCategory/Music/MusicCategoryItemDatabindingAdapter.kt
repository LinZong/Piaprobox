package com.nemesiss.dev.piaprobox.Adapter.SubmissionWorkCategory.Music

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.nemesiss.dev.contentparser.model.RecommendItemModel
import com.nemesiss.dev.piaprobox.Adapter.Common.InfinityLoadAdapter
import com.nemesiss.dev.piaprobox.Adapter.RecommendPage.MusicRecommendItemDatabindingAdapter
import com.nemesiss.dev.piaprobox.model.resources.RecyclerViewInnerIndicator
import com.nemesiss.dev.piaprobox.view.common.detectWhichViewHolderToCreate

class MusicCategoryItemDatabindingAdapter(
    var items: List<RecommendItemModel>,
    private inline val itemSelected: (Int) -> Unit,
    attachedRecyclerView: RecyclerView,
    ShouldLoadMoreItem: () -> Unit
) : InfinityLoadAdapter<RecyclerView.ViewHolder>(
    attachedRecyclerView, ShouldLoadMoreItem
) {

    override fun getItemViewType(position: Int): Int {
        return RecyclerViewInnerIndicator.values().find { indicator -> indicator.TAG == items[position].URL }?.FLAG ?: 0
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        detectWhichViewHolderToCreate(
            MusicRecommendItemDatabindingAdapter.RecommendItemDatabindingVH::class
        )(viewGroup, viewType)

    override fun getItemCount(): Int = items.size


    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, position: Int) {
        if (vh is MusicRecommendItemDatabindingAdapter.RecommendItemDatabindingVH) {
            vh.binding.model = items[position]
            vh.binding.root.setOnClickListener { itemSelected(position) }
        }
    }
}