package com.nemesiss.dev.piaprobox.Adapter.RecommendPage

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModel
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.GlideApp
import com.nemesiss.dev.piaprobox.View.Common.fixThumb

class MusicRecommendItemAdapter(
    var items: List<RecommendItemModel>,
    val context: Context,
    inline val itemSelected: (Int) -> Unit
) : RecyclerView.Adapter<MusicRecommendItemAdapter.RecommendItemVH>() {

    class RecommendItemVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val Thumb: ImageView = itemView.findViewById(R.id.SingleWorkItemCard_WorkThumb)
        val WorkName: TextView = itemView.findViewById(R.id.SingleWorkItemCard_WorkName)
        val UploadUser: TextView = itemView.findViewById(R.id.SingleWorkItemCard_UploadUser)
        val UploadTime: TextView = itemView.findViewById(R.id.SingleWorkItemCard_UploadTime)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, index: Int): RecommendItemVH {
        return RecommendItemVH(
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.single_recommend_item_no_databinding, viewGroup, false)
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(vh: RecommendItemVH, index: Int) {
        val current = items[index]
        vh.itemView.setOnClickListener { itemSelected(index) }
        vh.WorkName.text = current.ItemName
        vh.UploadUser.text = current.ArtistName
        vh.UploadTime.text = "${current.UploadDate} ${current.UploadTime}"

        if (!current.fixThumb(vh.Thumb)) {
            GlideApp
                .with(context)
                .load(current.Thumb)
                .fallback(R.drawable.thumb_miku)
                .into(vh.Thumb)
        }
    }
}