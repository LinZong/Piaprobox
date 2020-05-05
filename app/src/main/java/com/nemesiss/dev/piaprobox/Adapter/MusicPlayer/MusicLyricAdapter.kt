package com.nemesiss.dev.piaprobox.Adapter.MusicPlayer

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nemesiss.dev.piaprobox.Application.PiaproboxApplication
import com.nemesiss.dev.piaprobox.R
import kotlinx.android.synthetic.main.single_lyric_item.view.*

class MusicLyricAdapter(var items: List<String>) : RecyclerView.Adapter<MusicLyricAdapter.MusicLyricViewHolder>() {
    class MusicLyricViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(viewGroup: ViewGroup, index: Int): MusicLyricViewHolder {

        return MusicLyricViewHolder(
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.single_lyric_item, viewGroup, false)
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(vh: MusicLyricViewHolder, index: Int) {
        vh.itemView.MusicPlayer_Lyric_Item.text = items[index]
    }

    companion object {
        @JvmStatic
        fun BuildNoLyricList(): List<String> {
            return arrayListOf<String>(PiaproboxApplication.Self.applicationContext.resources.getString(R.string.NoLyricTips))
        }
    }
}