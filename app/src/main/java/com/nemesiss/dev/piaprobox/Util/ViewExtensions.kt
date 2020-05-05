package com.nemesiss.dev.piaprobox.Util

import android.view.View
import android.widget.ImageView
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModel
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModelImage
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModelText
import com.nemesiss.dev.piaprobox.Misc.RecyclerViewInnerIndicator
import com.nemesiss.dev.piaprobox.Misc.StaticResourcesMap
import com.nemesiss.dev.piaprobox.R


// 简单的点击事件绑定操作

fun <T : View> List<T>.whenClicks(vararg clickHandler: (View) -> Unit) {
    zip(clickHandler)
        .forEach { (view, listener) -> view.setOnClickListener { listener(it) } }
}

fun <T : View> List<T>.whenClicks(vararg clickHandler: View.OnClickListener) {
    zip(clickHandler)
        .forEach { (view, listener) -> view.setOnClickListener(listener) }
}

// 分类条目RecyclerView展示项相关操作

fun RecommendItemModel.isLoadMoreIndicator() =
    this.URL == RecyclerViewInnerIndicator.RECYCLER_VIEW_LOAD_MORE_INDICATOR.TAG

fun RecommendItemModelImage.isLoadMoreIndicator() =
    this.URL == RecyclerViewInnerIndicator.RECYCLER_VIEW_LOAD_MORE_INDICATOR.TAG

fun RecommendItemModelText.isLoadMoreIndicator() =
    this.URL == RecyclerViewInnerIndicator.RECYCLER_VIEW_LOAD_MORE_INDICATOR.TAG

fun RecommendItemModel.isNoMoreIndicator() =
    this.URL == RecyclerViewInnerIndicator.RECYCLER_VIEW_NOTHING_MORE_INDICATOR.TAG

fun RecommendItemModelImage.isNoMoreIndicator() =
    this.URL == RecyclerViewInnerIndicator.RECYCLER_VIEW_NOTHING_MORE_INDICATOR.TAG

fun RecommendItemModelText.isNoMoreIndicator() =
    this.URL == RecyclerViewInnerIndicator.RECYCLER_VIEW_NOTHING_MORE_INDICATOR.TAG

fun RecommendItemModel.fixThumb(imageView: ImageView) : Boolean {
    if (Thumb.matches("^th-.*".toRegex())) {
        imageView.setImageResource(StaticResourcesMap.DefaultThumbMaps[Thumb] ?: R.drawable.thumb_empty)
        return true
    }
    return false
}

fun RecommendItemModelText.fixThumb(imageView: ImageView): Boolean {
    if (Thumb.matches("^th-.*".toRegex())) {
        imageView.setImageResource(StaticResourcesMap.DefaultThumbMaps[Thumb] ?: R.drawable.thumb_empty)
        return true
    }
    return false
}

inline fun <reified T> MutableList<T>.removeIndicator(): Int {
    var removedIndex = -1
    when (T::class) {
        RecommendItemModel::class, RecommendItemModelText::class, RecommendItemModelImage::class -> {
            val URL_Field = T::class.java.getDeclaredField("URL")
            for (index in indices.reversed()) {
                if (URL_Field.get(this[index]) == RecyclerViewInnerIndicator.RECYCLER_VIEW_LOAD_MORE_INDICATOR.TAG) {
                    removeAt(index)
                    removedIndex = index
                    break
                }
                if (URL_Field.get(this[index]) == RecyclerViewInnerIndicator.RECYCLER_VIEW_NOTHING_MORE_INDICATOR.TAG) {
                    removeAt(index)
                    removedIndex = index
                    break
                }
            }
        }
    }
    return removedIndex
}