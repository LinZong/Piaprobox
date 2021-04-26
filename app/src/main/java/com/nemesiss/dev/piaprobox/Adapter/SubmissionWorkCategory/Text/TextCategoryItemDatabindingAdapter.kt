package com.nemesiss.dev.piaprobox.Adapter.SubmissionWorkCategory.Text

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.nemesiss.dev.contentparser.model.RecommendItemModelText
import com.nemesiss.dev.piaprobox.Adapter.Common.InfinityLoadAdapter
import com.nemesiss.dev.piaprobox.Adapter.RecommendPage.TextRecommendItemDatabindingAdapter
import com.nemesiss.dev.piaprobox.view.common.detectWhichViewHolderToCreate

class TextCategoryItemDatabindingAdapter(
    var items: List<RecommendItemModelText>,
    private inline val itemSelected: (Int) -> Unit,
    attachedRecyclerView: RecyclerView,
    ShouldLoadMoreItem: () -> Unit
) :
    InfinityLoadAdapter<RecyclerView.ViewHolder>(
        attachedRecyclerView, ShouldLoadMoreItem
    ) {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return detectWhichViewHolderToCreate(TextRecommendItemDatabindingAdapter.TextRecommendItemVH::class)(
            viewGroup,
            viewType
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, position: Int) {
        if (vh is TextRecommendItemDatabindingAdapter.TextRecommendItemVH) {
            vh.binding.model = items[position]
            vh.binding.root.setOnClickListener {
                itemSelected(
                    position
                )
            }
        }
    }
}