package com.nemesiss.dev.piaprobox.Adapter.RecommendPage

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.nemesiss.dev.contentparser.model.RecommendItemModelText
import com.nemesiss.dev.piaprobox.databinding.SingleRecommendItemTextBinding

class TextRecommendItemDatabindingAdapter(
    var items: List<RecommendItemModelText>,
    val context: Context,
    inline val itemSelected: (Int) -> Unit
) : RecyclerView.Adapter<TextRecommendItemDatabindingAdapter.TextRecommendItemVH>() {

    class TextRecommendItemVH private constructor(val binding: SingleRecommendItemTextBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            @JvmStatic
            fun create(viewGroup: ViewGroup): TextRecommendItemVH {
                return TextRecommendItemVH(
                    SingleRecommendItemTextBinding.inflate(
                        LayoutInflater.from(viewGroup.context),
                        viewGroup,
                        false
                    )
                )
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, index: Int) =
        TextRecommendItemVH.create(viewGroup)

    override fun getItemCount() = items.size

    override fun onBindViewHolder(vh: TextRecommendItemVH, index: Int) {
        val current = items[index]
        vh.itemView.setOnClickListener { itemSelected(index) }
        vh.binding.model = current
    }
}