package com.nemesiss.dev.piaprobox.Adapter.RecommendPage

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModel
import com.nemesiss.dev.piaprobox.databinding.SingleRecommendItemBinding

class MusicRecommendItemDatabindingAdapter(
    var items: List<RecommendItemModel>,
    val context: Context,
    inline val itemSelected: (Int) -> Unit
) : RecyclerView.Adapter<MusicRecommendItemDatabindingAdapter.RecommendItemDatabindingVH>() {


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

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecommendItemDatabindingVH =
        RecommendItemDatabindingVH.create(viewGroup)

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(vh: RecommendItemDatabindingVH, position: Int) {
        vh.binding.model = items[position]

        vh.binding.root.setOnClickListener { itemSelected(position) }
    }
}