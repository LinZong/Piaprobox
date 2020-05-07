package com.nemesiss.dev.piaprobox.View.Common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Util.AppUtil

fun getColorSteps(R: Int, G: Int, B: Int): FloatArray {
    val rgbRank = listOf(0, 1, 2)
        .zip(listOf(R, G, B))
        .sortedByDescending { (_, color) -> color }

    val colorSteps = FloatArray(3)
    colorSteps[rgbRank[0].first] = (255 - rgbRank[0].second).toFloat() / 255
    colorSteps[rgbRank[1].first] = (255 - rgbRank[1].second).toFloat() / 255
    colorSteps[rgbRank[2].first] = (255 - rgbRank[2].second).toFloat() / 255
    return colorSteps
}

fun getColorSteps(colorInt: Int): FloatArray {
    return getColorSteps(Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt))
}

class LoadingLineIndicator : View {
    private val defaultWidth = AppUtil.Dp2Px(resources, 150)
    private val defaultHeight = AppUtil.Dp2Px(resources, 10)
    private val oneRunDuration = 600
    private var mWidth: Int = defaultWidth
    private var mHeight: Int = defaultHeight
    private var mProgressWidth = 0.2 * mWidth
    private var colorChangeStep = 255f / oneRunDuration
    private var widthChangeStep = (0.8 * mWidth) / oneRunDuration
    private val initColor = resources.getColor(R.color.PiaproColor)
    private var currColor = initColor
    private val colorRGBStep = getColorSteps(initColor)

    @Volatile
    private var mVisibility = VISIBLE
        set(value) {
            field = value
            if (field == VISIBLE) {
                invalidateHandler.removeMessages(1)
                invalidateHandler.sendEmptyMessageDelayed(1, REFRESH_RATE.toLong())
            }
        }

    private val REFRESH_RATE = (1000f) / 60

    private
    val paint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        isAntiAlias = true
    }

    private val invalidateHandler = Handler(this::handleInvalidateViewMsg)

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mWidth = commonMeasureDimension(widthMeasureSpec, defaultWidth)
        mHeight = commonMeasureDimension(heightMeasureSpec, defaultHeight)
        mProgressWidth = 0.2 * mWidth
        widthChangeStep = (0.8 * mWidth) / oneRunDuration

        Log.d("LoadingLineIndicator", "测量宽高: $mWidth, $mHeight  $mProgressWidth  $widthChangeStep")

        paint.strokeWidth = mHeight.toFloat()
        setMeasuredDimension(
            mWidth, mHeight
        )
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        mVisibility = visibility
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        invalidateHandler.sendEmptyMessageDelayed(1, REFRESH_RATE.toLong())
    }

    private fun handleInvalidateViewMsg(message: Message): Boolean {
        invalidate()
        if (mVisibility == VISIBLE)
            invalidateHandler.sendEmptyMessageDelayed(1, REFRESH_RATE.toLong())
        return true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
        paint.strokeWidth = mHeight.toFloat()
        mProgressWidth = 0.2 * mWidth
        widthChangeStep = (0.8 * mWidth) / oneRunDuration
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.color = currColor
        canvas?.drawLine(
            (mWidth - mProgressWidth).toFloat() / 2,
            0f,
            (mWidth + mProgressWidth).toFloat() / 2,
            0f,
            paint
        )
        updateWidthAndColorByStep()
    }

    private fun updateWidthAndColorByStep() {

        val NextR = (Color.red(currColor) + colorRGBStep[0] * colorChangeStep * REFRESH_RATE).toInt().coerceAtMost(255)
        val NextG =
            (Color.green(currColor) + colorRGBStep[1] * colorChangeStep * REFRESH_RATE).toInt().coerceAtMost(255)
        val NextB = (Color.blue(currColor) + colorRGBStep[2] * colorChangeStep * REFRESH_RATE).toInt().coerceAtMost(255)


        currColor = Color.rgb(NextR, NextG, NextB)
        mProgressWidth += (widthChangeStep * REFRESH_RATE)

        // 重设到初始状态.
        if (mProgressWidth > mWidth) {
            mProgressWidth = 0.2 * mWidth
            currColor = initColor
        }
        Log.d("LoadingLineIndicator", "下一组颜色: $NextR  $NextG  $NextB, 下一组长度: $mProgressWidth")
    }
}