package com.nemesiss.dev.piaprobox.view.common

import android.content.Context
import android.databinding.DataBindingUtil
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ScrollView
import com.nemesiss.dev.piaprobox.databinding.TextReaderLayoutBinding
import kotlinx.android.synthetic.main.text_reader_layout.view.*

class PinchScaleTextReader : ScrollView, ScaleGestureDetector.OnScaleGestureListener {

    private lateinit var binding: TextReaderLayoutBinding
    private lateinit var scaleDetector : ScaleGestureDetector
    constructor(context: Context?) : super(context) {
        InitView()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){
        InitView()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        InitView()
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        InitView()
    }
    private fun InitView() {
        scaleDetector = ScaleGestureDetector(context,this)

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        binding = DataBindingUtil.findBinding(this)!!
    }


    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        // Let the gesture detector handle the touch event.
        super.onTouchEvent(ev)
        return scaleDetector.onTouchEvent(ev)
    }

    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
    }

    override fun onScale(detector: ScaleGestureDetector?): Boolean {
        val factor = detector?.scaleFactor ?: 1f
        TextReader_Content.setTextSize(TypedValue.COMPLEX_UNIT_PX, TextReader_Content.textSize * factor)
        return true
    }
}