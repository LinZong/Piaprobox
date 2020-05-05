package com.nemesiss.dev.piaprobox.Adapter.RecommendPage

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModelImage
import com.nemesiss.dev.piaprobox.databinding.SingleRecommendImageItemBinding
import kotlinx.android.synthetic.main.single_recommend_image_item.view.*

class ImageRecommendItemAdapter(
    var items: List<RecommendItemModelImage>,
    val context: Context,
    inline val OnItemSelected: (Int, ImageView) -> Unit
) : RecyclerView.Adapter<ImageRecommendItemAdapter.ImageRecommendItemViewHolder>() {

    class ImageRecommendItemViewHolder private constructor(val binding: SingleRecommendImageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun create(viewGroup: ViewGroup): ImageRecommendItemViewHolder {
                val binding = SingleRecommendImageItemBinding
                    .inflate(
                        LayoutInflater.from(viewGroup.context),
                        viewGroup, false
                    )
                return ImageRecommendItemViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, index: Int) = ImageRecommendItemViewHolder.create(viewGroup)

    override fun getItemCount() = items.size

    override fun onBindViewHolder(vh: ImageRecommendItemViewHolder, index: Int) {
        val item = items[index]
        vh.itemView.setOnClickListener { OnItemSelected(index, vh.itemView.SingleImageWorkItemCard_WorkThumb) }
        // Model binding
        vh.binding.model = item
    }
}