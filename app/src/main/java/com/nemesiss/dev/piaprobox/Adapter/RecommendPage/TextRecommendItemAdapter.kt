package com.nemesiss.dev.piaprobox.Adapter.RecommendPage

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModelText
import com.nemesiss.dev.piaprobox.Misc.StaticResourcesMap
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.HTMLParser
import kotlinx.android.synthetic.main.single_recommend_item.view.*
import java.lang.Exception


class TextRecommendItemAdapter(
    var items: List<RecommendItemModelText>,
    val context: Context,
    val itemSelected: (Int) -> Unit
) : RecyclerView.Adapter<TextRecommendItemAdapter.TextRecommendItemVH>() {

    class TextRecommendItemVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val Thumb: ImageView = itemView.findViewById(R.id.SingleWorkItemCard_WorkThumb)
        val WorkName: TextView = itemView.findViewById(R.id.SingleWorkItemCard_WorkName)
        val UploadUser: TextView = itemView.findViewById(R.id.SingleWorkItemCard_UploadUser)
        val UploadTime: TextView = itemView.findViewById(R.id.SingleWorkItemCard_UploadTime)
        val Opening : TextView = itemView.SingleWorkItemCard_Opening
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, index: Int): TextRecommendItemVH {
        return TextRecommendItemVH(
            LayoutInflater.from(viewGroup.context).inflate(
                R.layout.single_recommend_item,
                viewGroup,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(vh: TextRecommendItemVH, index: Int) {
        val current = items[index]
        vh.itemView.setOnClickListener { itemSelected(index) }
        vh.WorkName.text = current.ItemName
        vh.UploadUser.text = current.ArtistName
        vh.UploadTime.text = current.UploadDate
        vh.Opening.text = current.Opening

        val LoadedImage = HTMLParser.GetAlbumThumb(current.Thumb)

        Log.d("TextRecommendItem", LoadedImage)

        Glide.with(context)
            .load(LoadedImage)
            .priority(Priority.NORMAL)
            .error(R.drawable.thumb_miku)
            .into(vh.Thumb)
    }
}