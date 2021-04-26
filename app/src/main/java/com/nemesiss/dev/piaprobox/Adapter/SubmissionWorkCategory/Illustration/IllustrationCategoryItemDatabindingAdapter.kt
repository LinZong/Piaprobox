package com.nemesiss.dev.piaprobox.Adapter.SubmissionWorkCategory.Illustration

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.ImageView
import com.nemesiss.dev.contentparser.model.RecommendItemModelImage
import com.nemesiss.dev.piaprobox.Adapter.Common.InfinityLoadAdapter
import com.nemesiss.dev.piaprobox.Adapter.RecommendPage.ImageRecommendItemDatabindingAdapter
import com.nemesiss.dev.piaprobox.model.resources.RecyclerViewInnerIndicator
import com.nemesiss.dev.piaprobox.view.common.detectWhichViewHolderToCreate

class IllustrationCategoryItemDatabindingAdapter(
    var items: List<RecommendItemModelImage>,
    private inline val itemSelected: (Int, ImageView) -> Unit,
    attachedRecyclerView: RecyclerView,
    ShouldLoadMoreItem: () -> Unit
) :
    InfinityLoadAdapter<RecyclerView.ViewHolder>(
        attachedRecyclerView, ShouldLoadMoreItem
    ) {

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, position: Int) {
        if (vh is ImageRecommendItemDatabindingAdapter.ImageRecommendItemViewHolder) {
            vh.binding.model = items[position]
            vh.binding.root.setOnClickListener { itemSelected(position, vh.binding.SingleImageWorkItemCardWorkThumb) }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return RecyclerViewInnerIndicator.values().find { indicator -> indicator.TAG == items[position].URL }?.FLAG ?: 0
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        detectWhichViewHolderToCreate(
            ImageRecommendItemDatabindingAdapter.ImageRecommendItemViewHolder::class
        )(viewGroup, viewType)

}
