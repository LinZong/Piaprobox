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
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModel
import com.nemesiss.dev.piaprobox.Misc.StaticResourcesMap
import com.nemesiss.dev.piaprobox.R

class RecommendItemAdapter(var items : List<RecommendItemModel>, val context: Context) : RecyclerView.Adapter<RecommendItemAdapter.RecommendItemVH>() {

    class RecommendItemVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val Thumb : ImageView = itemView.findViewById(R.id.SingleWorkItemCard_WorkThumb)
        val WorkName : TextView = itemView.findViewById(R.id.SingleWorkItemCard_WorkName)
        val UploadUser : TextView = itemView.findViewById(R.id.SingleWorkItemCard_UploadUser)
        val UploadTime : TextView = itemView.findViewById(R.id.SingleWorkItemCard_UploadTime)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, index: Int): RecommendItemVH {
        return RecommendItemVH(LayoutInflater.from(viewGroup.context).inflate(R.layout.single_recommend_item, viewGroup,false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(vh: RecommendItemVH, index: Int) {
        val current = items[index]
        vh.WorkName.text = current.ItemName
        vh.UploadUser.text = current.ArtistName
        vh.UploadTime.text = "${current.UploadDate} ${current.UploadTime}"
        if(current.Thumb.matches("^th-.*".toRegex())) {
            vh.Thumb.setImageResource(StaticResourcesMap.DefaultThumbMaps[current.Thumb] ?: R.drawable.thumb_empty)
        }
        else {
            Glide.with(context)
                .load(current.Thumb)
                .priority(Priority.HIGH)
                .error(R.drawable.thumb_miku)
                .into(vh.Thumb)
        }
    }
}