package com.nemesiss.dev.piaprobox.View.Common

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nemesiss.dev.piaprobox.R

class RecyclerViewLoadingIndicatorViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    companion object {
        @JvmStatic
        fun create(viewGroup: ViewGroup): RecyclerViewLoadingIndicatorViewHolder {
            return RecyclerViewLoadingIndicatorViewHolder(
                LayoutInflater.from(
                    viewGroup.context
                ).inflate(R.layout.recyclerview_loadmore_indicator, viewGroup, false)
            )
        }
    }
}