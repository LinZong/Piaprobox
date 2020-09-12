package com.nemesiss.dev.piaprobox.View.Common

import android.content.Intent
import android.graphics.Matrix
import android.support.v4.app.SharedElementCallback
import android.support.v7.widget.RecyclerView
import android.transition.Transition
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModel
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModelImage
import com.nemesiss.dev.HTMLContentParser.Model.RecommendItemModelText
import com.nemesiss.dev.piaprobox.Activity.Common.PiaproboxBaseActivity
import com.nemesiss.dev.piaprobox.Activity.Image.IllustratorViewActivity2
import com.nemesiss.dev.piaprobox.Adapter.RecommendPage.ImageRecommendItemDatabindingAdapter
import com.nemesiss.dev.piaprobox.Fragment.HomePage.Recommend.Categories.BaseRecommendCategoryFragment
import com.nemesiss.dev.piaprobox.Misc.RecyclerViewInnerIndicator
import com.nemesiss.dev.piaprobox.Misc.StaticResourcesMap
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Util.BaseTransitionCallback
import com.nemesiss.dev.piaprobox.Util.MediaSharedElementCallback
import kotlinx.android.synthetic.main.single_recommend_image_item.view.*
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.reflect.KClass


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

fun RecommendItemModel.fixThumb(imageView: ImageView): Boolean {
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

inline fun <reified T> canAddIndicator(): Boolean {
    return when (T::class) {
        RecommendItemModel::class, RecommendItemModelText::class, RecommendItemModelImage::class -> {
            true
        }
        else -> {
            false
        }
    }
}

inline fun <reified T> MutableList<T>.removeIndicator(): Int {
    var removedIndex = -1
    if (canAddIndicator<T>()) {
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
    return removedIndex
}


inline fun <reified T : RecyclerView.ViewHolder> detectWhichViewHolderToCreate(shouldCreateVH: KClass<T>)
        : (ViewGroup, Int) -> RecyclerView.ViewHolder {
    return { viewGroup: ViewGroup, viewType: Int ->
        when (viewType) {
            RecyclerViewInnerIndicator.RECYCLER_VIEW_LOAD_MORE_INDICATOR.FLAG
            -> RecyclerViewLoadingIndicatorViewHolder.create(
                    viewGroup
            )
            RecyclerViewInnerIndicator.RECYCLER_VIEW_NOTHING_MORE_INDICATOR.FLAG
            -> RecyclerViewNothingMoreIndicatorViewHolder.create(
                    viewGroup
            )
            else -> {
                val createVHMethod = shouldCreateVH.java.getMethod("create", ViewGroup::class.java)
                createVHMethod.invoke(null, viewGroup) as RecyclerView.ViewHolder
            }
        }
    }
}

inline fun BaseRecommendCategoryFragment.handleSharedElementReenter(
        recyclerView: RecyclerView,
        crossinline rollToCorrectPos: () -> Int
): (Int, Intent?) -> Unit {

    return { resultCode, _ ->
        when (resultCode) {
            // 处理来自IllustratorViewActivity2的Re-enter.
            IllustratorViewActivity2.REENTER_RESULT_CODE -> {
                val position = rollToCorrectPos()
                val sharedElementCallback = MediaSharedElementCallback()
                activity?.setExitSharedElementCallback(sharedElementCallback)
                activity?.window?.sharedElementExitTransition?.addListener(object : BaseTransitionCallback() {
                    override fun onTransitionEnd(p0: Transition?) {
                        removeCallback()
                    }

                    override fun onTransitionCancel(p0: Transition?) {
                        removeCallback()
                    }

                    private fun removeCallback() {
                        if (activity != null) {
                            activity!!.window.sharedElementExitTransition.removeListener(this)
                            activity!!.setExitSharedElementCallback(object : SharedElementCallback() {})
                        }
                    }
                })
                activity?.supportPostponeEnterTransition()
                recyclerView.viewTreeObserver.addOnPreDrawListener(object :
                        ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        val vh = recyclerView.findViewHolderForAdapterPosition(position)
                        if (vh != null && vh is ImageRecommendItemDatabindingAdapter.ImageRecommendItemViewHolder) {
                            sharedElementCallback.setSharedElementViews(
                                    arrayOf(resources.getString(R.string.ImageViewTransitionName)),
                                    arrayOf(vh.itemView.SingleImageWorkItemCard_WorkThumb)
                            )
                            recyclerView.viewTreeObserver.removeOnPreDrawListener(this)
                            activity?.supportStartPostponedEnterTransition()
                            // 可以清除CallbackListener.
                        }
                        return true
                    }
                })
            }
        }
    }
}

fun commonMeasureDimension(measureSpec: Int, defaultSize: Int): Int {
    val size = View.MeasureSpec.getSize(measureSpec)

    return when (View.MeasureSpec.getMode(measureSpec)) {
        View.MeasureSpec.AT_MOST -> defaultSize.coerceAtMost(size)
        View.MeasureSpec.EXACTLY -> size
        View.MeasureSpec.UNSPECIFIED -> defaultSize
        else -> defaultSize
    }
}

private fun calculateAlpha(currHypo: Double, maxHypo: Double): Int {
    return (255 - (currHypo * 255 / maxHypo)).coerceAtLeast(0.0).toInt()
}

private fun calculateDeltaDistance(event: MotionEvent, baseX: Float, baseY: Float): Float {
    return hypot(event.rawX + baseX, event.rawY + baseY)
}

fun PinchImageView.isScaling(): Boolean {
    val matrix = Matrix()
    getOuterMatrix(matrix)
    val matrixValues = FloatArray(9)
    matrix.getValues(matrixValues)
    val scaleX = matrixValues[0]
    val scaleY = matrixValues[4]
    return scaleX > 1.03 || scaleY > 1.03
}

private fun isMovingVertical(distanceY: Float) : Boolean {
    return abs(distanceY) > 20
}

fun PiaproboxBaseActivity.wrapDragAndCloseTouchHandler(imageView: PinchImageView, backgroundView: View, closeThreshold: Int) {
    imageView.post {
        val originalX = imageView.x
        val originalY = imageView.y
        val maxHypo = hypot(0.0, (imageView.height / 2).toDouble())
        var baseDistanceX = 0.0f
        var baseDistanceY = 0.0f

        backgroundView.background.alpha = 255


        // 要求只有不缩放，并且是向下方划的时候才触发
        imageView.addOnTouchListener handleMoveClose@{ _, event ->

            if (imageView.pinchMode == PinchImageView.PINCH_MODE_SCALE || imageView.isScaling()) {
                imageView.isDisablePinchGesture = false
                return@handleMoveClose false
            }

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    baseDistanceX = imageView.x - event.rawX
                    baseDistanceY = imageView.y - event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    val distanceY: Float = event.rawY + baseDistanceY
                    if(isMovingVertical(distanceY)) {
                        if(!imageView.isDisablePinchGesture) {
                            imageView.isDisablePinchGesture = true
                        }
                        val hypo = hypot(0f, distanceY).toDouble()
                        backgroundView.background.alpha = calculateAlpha(hypo, maxHypo)
                        imageView.animate().y(distanceY).setDuration(0).start()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (imageView.isDisablePinchGesture) {
                        val finalDistance = calculateDeltaDistance(event, baseDistanceX, baseDistanceY)
                        if (finalDistance > closeThreshold) {
                            supportFinishAfterTransition()
                        } else {
                            imageView.animate().y(originalY).setDuration(100).start()
                            backgroundView.background.alpha = 255
                        }
                    }
                    imageView.isDisablePinchGesture = false
                }
            }
            return@handleMoveClose true
        }
    }
}