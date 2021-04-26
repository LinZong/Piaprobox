package com.nemesiss.dev.piaprobox.Adapter.MusicPlayer

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.nemesiss.dev.contentparser.model.RelatedMusicInfo
import com.nemesiss.dev.piaprobox.Application.PiaproboxApplication
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.HTMLParser.Companion.GetAlbumThumb
import kotlinx.android.synthetic.main.single_related_music_item.view.*

class RelatedMusicListAdapter(var items : List<RelatedMusicInfo>, val itemSelected : (Int)->Unit) : RecyclerView.Adapter<RelatedMusicListAdapter.RelatedMusicListViewHolder>() {

    class RelatedMusicListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(viewGroup: ViewGroup, index: Int): RelatedMusicListViewHolder {
        return RelatedMusicListViewHolder(
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.single_related_music_item,viewGroup,false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(vh: RelatedMusicListViewHolder, index: Int) {
        val item = items[index]
        Glide
            .with(PiaproboxApplication.Self.applicationContext)
            .load(GetAlbumThumb(item.Thumb))
            .into(vh.itemView.MusicPlayer_RelatedMusic_Item_Thumb)
        vh.itemView.setOnClickListener { itemSelected(index) }
        vh.itemView.MusicPlayer_RelatedMusic_Item_Title.text = item.Title
        vh.itemView.MusicPlayer_RelatedMusic_Item_Artist.text = item.Artist

        // TODO Bind onClick Events
    }
}