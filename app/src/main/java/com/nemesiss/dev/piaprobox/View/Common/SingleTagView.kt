package com.nemesiss.dev.piaprobox.View.Common

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.single_tag.view.*

class SingleTagView : RelativeLayout {
    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )
    fun SetSelected() {
        MainFragment_TagText.isSelected = true
    }

    fun SetDeSelected() {
        MainFragment_TagText.isSelected = false
    }
}