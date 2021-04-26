package com.nemesiss.dev.piaprobox.view.common

import android.content.Context
import android.support.v7.widget.GridLayoutManager

class GridLayoutManagerWithBottomIndicator(
    context: Context?,
    spanCount: Int,
    private inline val spanSizeJudge: (Int) -> Int
) :
    GridLayoutManager(
        context,
        spanCount
    ) {

    init {
        spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return spanSizeJudge(position)
            }
        }
    }
}