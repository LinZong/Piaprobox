package com.nemesiss.dev.piaprobox.Adapter.RecommendPage

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModelImage
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.HTMLParser
import kotlinx.android.synthetic.main.single_recommend_image_item.view.*

class ImageRecommendItemAdapter(
    var items: List<RecommendItemModelImage>,
    val context: Context,
    val OnItemSelected: (Int, ImageView) -> Unit
) : RecyclerView.Adapter<ImageRecommendItemAdapter.ImageRecommendItemViewHolder>() {

    class ImageRecommendItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(viewGroup: ViewGroup, index: Int): ImageRecommendItemViewHolder {
        return ImageRecommendItemViewHolder(
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.single_recommend_image_item, viewGroup, false)
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(vh: ImageRecommendItemViewHolder, index: Int) {
        val item = items[index]

        Log.d("ImageAdapter","Load :${item.Thumb}  ${item.ArtistAvatar}")
        vh.itemView.setOnClickListener { OnItemSelected(index, vh.itemView.SingleImageWorkItemCard_WorkThumb) }
        Glide.with(context)
            .load(HTMLParser.GetAlbumThumb(item.Thumb))
            .priority(Priority.HIGH)
            .into(vh.itemView.SingleImageWorkItemCard_WorkThumb)

        Glide.with(context)
            .load(HTMLParser.GetAlbumThumb(item.ArtistAvatar))
            .priority(Priority.HIGH)
            .into(vh.itemView.SingleImageWorkItemCard_UploadUser_Avatar)

        vh.itemView.SingleImageWorkItemCard_UploadUser.text = item.ArtistName
        vh.itemView.SingleImageWorkItemCard_UploadTime.text = item.UploadDate
    }
}