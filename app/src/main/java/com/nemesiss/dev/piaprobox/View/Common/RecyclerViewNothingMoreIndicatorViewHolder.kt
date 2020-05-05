package com.nemesiss.dev.piaprobox.View.Common

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nemesiss.dev.piaprobox.R

class RecyclerViewNothingMoreIndicatorViewHolder private constructor(view: View) : RecyclerView.ViewHolder(view) {
    companion object {
        @JvmStatic
        fun create(viewGroup: ViewGroup): RecyclerViewNothingMoreIndicatorViewHolder {
            return RecyclerViewNothingMoreIndicatorViewHolder(
                LayoutInflater.from(
                    viewGroup.context
                ).inflate(R.layout.recyclerview_nomore_indicator, viewGroup, false)
            )
        }
    }
}