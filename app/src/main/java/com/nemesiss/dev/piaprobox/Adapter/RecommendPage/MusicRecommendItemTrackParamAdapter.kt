package com.nemesiss.dev.piaprobox.Adapter.RecommendPage

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModel
import com.nemesiss.dev.piaprobox.Activity.Music.MusicControlActivity
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.GlideApp
import com.nemesiss.dev.piaprobox.View.Common.fixThumb
import com.zzm.android_basic_library.model.tracker.AutoTrackConfig
import com.zzm.android_basic_library.model.tracker.TrackerViewHolderType
import com.zzm.android_basic_library.view.ClickableTrackerViewHolder
import com.zzm.android_basic_library.view.TrackerAdapter
import com.zzm.android_basic_library.view.TrackerViewHolder

class MusicRecommendItemTrackParamAdapter(
    var items: List<RecommendItemModel>,
    val context: Context,
    inline val itemSelected: (Int) -> Unit
) : TrackerAdapter<MusicRecommendItemTrackParamAdapter.RecommendItemVH>(
    AutoTrackConfig(
        "recommend_home",
        "music_list",
        500
    )
) {

    class RecommendItemVH(itemView: View) : ClickableTrackerViewHolder(itemView) {
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

        vh.dest = MusicControlActivity::class.simpleName
        vh.eventParams = current.ItemName
        vh.extData = current

        if (!current.fixThumb(vh.Thumb)) {
            GlideApp
                .with(context)
                .load(current.Thumb)
                .fallback(R.drawable.thumb_miku)
                .into(vh.Thumb)
        }
    }
}