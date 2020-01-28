package com.nemesiss.dev.piaprobox.View.Common

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.RelativeLayout
import com.nemesiss.dev.piaprobox.R

class LoadingIndicatorView : RelativeLayout {
    constructor(context: Context?) : super(context) {
        Init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        Init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        Init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        Init()
    }

    private fun Init() {
        LayoutInflater.from(context)
            .inflate(R.layout.fullscreen_loading, this, true)
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
//        Log.d("LoadingIndicatorView","Touched!")
        return true
    }
}