package com.nemesiss.dev.piaprobox.Adapter.Common

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

abstract class InfinityLoadAdapter<TViewHolder : RecyclerView.ViewHolder>(
    private val attachedRecyclerView: RecyclerView,
    protected inline val ShouldLoadMoreItem: () -> Unit
) : RecyclerView.Adapter<TViewHolder>() {

    var visibleThreshold = 3
    private var loading = false
    private var disabled = false

    init {
        val attachedLayoutManager = attachedRecyclerView.layoutManager
        attachedRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            @Synchronized
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (loading || disabled)
                    return

                val itemCount = attachedLayoutManager?.itemCount ?: -1
                if (itemCount == -1)
                    return
                when (attachedLayoutManager) {
                    is LinearLayoutManager -> {
                        val lastVisibleChildPos = attachedLayoutManager.findLastVisibleItemPosition()
                        if (lastVisibleChildPos + visibleThreshold >= itemCount) {
                            loading = true
                            ShouldLoadMoreItem()
                        }
                    }
                    is GridLayoutManager -> {
                        val lastVisibleChildPos = attachedLayoutManager.findLastVisibleItemPosition()
                        if (lastVisibleChildPos + visibleThreshold >= itemCount) {
                            loading = true
                            ShouldLoadMoreItem()
                        }
                    }
                }
            }
        })
    }

    fun loaded() {
        loading = false
    }

    fun disable() {
        disabled = true
    }

    fun enable() {
        disabled = false
    }
}