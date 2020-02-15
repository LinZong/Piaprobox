package com.nemesiss.dev.piaprobox.Adapter.RecommendPage

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModelText
import com.nemesiss.dev.piaprobox.databinding.SingleRecommendItemBinding

class TextRecommendItemAdapter(
    var items: List<RecommendItemModelText>,
    val context: Context,
    val itemSelected: (Int) -> Unit
) : RecyclerView.Adapter<TextRecommendItemAdapter.TextRecommendItemVH>() {

    class TextRecommendItemVH private constructor(val binding: SingleRecommendItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun create(viewGroup: ViewGroup): TextRecommendItemVH {
                return TextRecommendItemVH(
                    SingleRecommendItemBinding.inflate(
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