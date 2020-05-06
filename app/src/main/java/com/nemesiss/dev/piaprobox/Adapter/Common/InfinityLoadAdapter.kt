package com.nemesiss.dev.piaprobox.Adapter.Common

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log

abstract class InfinityLoadAdapter<TViewHolder : RecyclerView.ViewHolder>(
    attachedRecyclerView: RecyclerView,
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
                    is GridLayoutManager -> {
                        val lastVisibleChildPos = attachedLayoutManager.findLastVisibleItemPosition()
                        if (lastVisibleChildPos + visibleThreshold >= itemCount) {
                            Log.d("GridLayoutInfinity","应该加载更多!")
                            loading = true
                            attachedRecyclerView.post { ShouldLoadMoreItem() }
                        }
                    }
                    is LinearLayoutManager -> {
                        val lastVisibleChildPos = attachedLayoutManager.findLastVisibleItemPosition()
                        if (lastVisibleChildPos + visibleThreshold >= itemCount) {
                            loading = true
                            attachedRecyclerView.post { ShouldLoadMoreItem() }
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