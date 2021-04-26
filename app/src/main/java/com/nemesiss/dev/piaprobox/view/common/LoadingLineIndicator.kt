package com.nemesiss.dev.piaprobox.view.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.nemesiss.dev.piaprobox.model.resources.CryptonCharacterColorDefinition
import com.nemesiss.dev.piaprobox.Util.AppUtil


class LoadingLineIndicator : View {
    private val defaultWidth = AppUtil.Dp2Px(resources, 150)
    private val defaultHeight = AppUtil.Dp2Px(resources, 10)
    private val oneRunDuration = 600
    private var mWidth: Int = defaultWidth
    private var mHeight: Int = defaultHeight

    private var mProgressWidth = 0.2 * mWidth
    private var widthChangeStep = (0.8 * mWidth) / oneRunDuration


    private var currColorAlpha = 255
    private val alphaChangeStep = 255f / oneRunDuration
    private var characterIndex = 0
    private val initColor = CryptonCharacterColorDefinition.values()[characterIndex].ColorInt


    @Volatile
    private var mVisibility = VISIBLE
        set(value) {
            field = value
            if (field == VISIBLE) {
                resetDrawingOptions()
                invalidateHandler.removeMessages(1)
                invalidateHandler.sendEmptyMessageDelayed(1, REFRESH_RATE.toLong())
            } else {
                removeDrawingLineActions()
            }
        }

    private val REFRESH_RATE = (1000f) / 60

    private
    val paint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        isAntiAlias = true
        color = initColor
        alpha = currColorAlpha
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
        if (changedView == this) {
            mVisibility = visibility
        }
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
        if(mVisibility == VISIBLE) {
            canvas?.drawLine(
                (mWidth - mProgressWidth).toFloat() / 2,
                0f,
                (mWidth + mProgressWidth).toFloat() / 2,
                0f,
                paint
            )
            updateWidthAndColorByStep()
        }
    }

    private fun updateWidthAndColorByStep() {

        currColorAlpha -= (alphaChangeStep * REFRESH_RATE).toInt().coerceAtLeast(0)
        mProgressWidth += (widthChangeStep * REFRESH_RATE)

        // 重设到初始状态.
        if (mProgressWidth > mWidth) {
            // next character color
            characterIndex = (characterIndex + 1) % CryptonCharacterColorDefinition.values().size
            paint.color = CryptonCharacterColorDefinition.values()[characterIndex].ColorInt
            resetDrawingOptions()
        }
        paint.alpha = currColorAlpha
    }

    @Synchronized
    private fun resetDrawingOptions() {
        mProgressWidth = 0.2 * mWidth
        currColorAlpha = 255
        paint.alpha = currColorAlpha
    }

    private fun removeDrawingLineActions() {
        invalidateHandler.removeMessages(1)
        invalidate()
    }
}