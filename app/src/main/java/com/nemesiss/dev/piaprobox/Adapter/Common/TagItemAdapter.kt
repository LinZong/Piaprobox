package com.nemesiss.dev.piaprobox.Adapter.Common

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nemesiss.dev.contentparser.model.RecommendTagModel
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.view.common.SingleTagView
import kotlinx.android.synthetic.main.single_tag.view.*

class TagItemAdapter(var items : List<RecommendTagModel>,val itemSelected : (Int)->Unit) : RecyclerView.Adapter<TagItemAdapter.TagItemVH>() {

    class TagItemVH(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(viewGroup: ViewGroup, index: Int): TagItemVH {
        return TagItemVH(
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.single_tag, viewGroup, false)
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(vh: TagItemVH, index: Int) {
        val root = vh.itemView
        if(index == 0) {
            (root as SingleTagView).SetSelected()
        }
        val item = items[index]
        root.MainFragment_TagText.text = item.Text
        root.MainFragment_TagText.setOnClickListener {
            Log.d("TagItemAdapter", "Clicked: $index ${item.Text}")
            itemSelected(index)
        }
    }
}