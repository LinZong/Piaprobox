package com.nemesiss.dev.piaprobox.Adapter.IllustratorViewer

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.nemesiss.dev.contentparser.model.RelatedImageInfo
import com.nemesiss.dev.piaprobox.databinding.SingleRelatedImageItemBinding

class RelatedImageListAdapter(
    var items: List<RelatedImageInfo>,
    private val OnRelatedItemSelected: (Int, RelatedImageInfo) -> Unit
) :
    RecyclerView.Adapter<RelatedImageListAdapter.RelatedImageItemViewHolder>() {
    class RelatedImageItemViewHolder private constructor(val binding: SingleRelatedImageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun create(viewGroup: ViewGroup): RelatedImageItemViewHolder {
                return RelatedImageItemViewHolder(
                    SingleRelatedImageItemBinding.inflate(
                        LayoutInflater.from(viewGroup.context),
                        viewGroup,
                        false
                    )
                )
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, index: Int): RelatedImageItemViewHolder {
        return RelatedImageItemViewHolder.create(viewGroup)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(viewHolder: RelatedImageItemViewHolder, index: Int) {
        viewHolder.itemView.setOnClickListener { OnRelatedItemSelected(index, items[index]) }
        viewHolder.binding.model = items[index]
    }
}