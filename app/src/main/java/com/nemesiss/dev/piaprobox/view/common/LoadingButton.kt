package com.nemesiss.dev.piaprobox.view.common

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.support.annotation.Dimension
import android.support.annotation.Dimension.PX
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Util.AppUtil
import com.nemesiss.dev.piaprobox.Util.SimpleAnimationListener
import kotlinx.android.synthetic.main.loading_button.view.*

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr), XmlConfigurableLayout {


    var mInnerIconSize: Int = 0
        private set
    var mProgressColor: Int = Color.WHITE
        private set

    private var mBackgroundImageView: ImageView

    private var mIconContainer: RelativeLayout

    private val lazyPendingIcon = lazy {
        ImageView(context)
            .apply {
                layoutParams = LayoutParams(mInnerIconSize, mInnerIconSize)
                    .apply {
                        addRule(CENTER_IN_PARENT, TRUE)
                    }
                setImageDrawable(context.getDrawable(R.drawable.baseline_arrow_forward_white_24dp))
                visibility = GONE
            }
    }

    private val lazyLoadingIcon = lazy {
        ProgressBar(context)
            .apply {
                layoutParams = LayoutParams(mInnerIconSize, mInnerIconSize)
                    .apply {
                        addRule(CENTER_IN_PARENT, TRUE)
                    }
                isIndeterminate = true
                indeterminateTintList = ColorStateList.valueOf(mProgressColor)
                visibility = GONE
            }
    }


    init {
        View.inflate(context, R.layout.loading_button, this)
        mBackgroundImageView = loading_button_background_iv
        mIconContainer = loading_button_icon_container
        obtainXmlConfig(context, attrs)
    }

    override fun obtainXmlConfig(context: Context, attrs: AttributeSet?) {
        context
            .obtainStyledAttributes(attrs, R.styleable.LoadingButton)
            .apply {
                setInnerIconSize(
                    getDimensionPixelSize(
                        R.styleable.LoadingButton_iconSize,
                        AppUtil.Dp2Px(context.resources, 36)
                    )
                )
                setProgressColor(getColor(R.styleable.LoadingButton_progressColor, Color.WHITE))
                setBackgroundSrc(getDrawable(R.styleable.LoadingButton_backgroundSrc))
                mIconContainer.addView(lazyPendingIcon.value)
                mIconContainer.addView(lazyLoadingIcon.value)
                when (getInt(R.styleable.LoadingButton_initMode, 0)) {
                    PENDING -> lazyPendingIcon.value.visibility = View.VISIBLE
                    LOADING -> lazyLoadingIcon.value.visibility = View.VISIBLE
                }
            }.recycle()
    }

    fun setInnerIconSize(@Dimension(unit = PX) size: Int) {
        mInnerIconSize = size
        lazyLoadingIcon.value.apply {
            val lp = layoutParams
            lp.width = size
            lp.height = size
            layoutParams = lp
        }
        lazyPendingIcon.value.apply {
            val lp = layoutParams
            lp.width = size
            lp.height = size
            layoutParams = lp
        }
    }

    fun setProgressColor(@ColorInt color: Int) {
        mProgressColor = color
        lazyLoadingIcon.value.indeterminateTintList = ColorStateList.valueOf(color)
    }

    fun setBackgroundSrc(drawable: Drawable?) {
        if (drawable != null) {
            mBackgroundImageView.setImageDrawable(drawable)
        }
    }

    fun pending() {
        switch(lazyLoadingIcon.value, lazyPendingIcon.value)
    }

    fun loading() {
        switch(lazyPendingIcon.value, lazyLoadingIcon.value)
    }

    fun disable() {
        mBackgroundImageView.apply {
            isClickable = false
            isFocusable = false
        }
    }

    fun enable() {
        mBackgroundImageView.apply {
            isClickable = true
            isFocusable = true
        }
    }

    private fun switch(viewExit: View, viewEnter: View) {
        val moveIn = AnimationUtils.loadAnimation(context, R.anim.move_in)
        val moveOut = AnimationUtils.loadAnimation(context, R.anim.move_out)
        viewEnter.visibility = View.VISIBLE
        viewExit.startAnimation(moveOut)
        viewEnter.startAnimation(moveIn)

        moveOut.setAnimationListener(object : SimpleAnimationListener() {
            override fun onAnimationEnd(animation: Animation?) {
                viewExit.visibility = GONE
            }
        })
    }

    /**
     * Delegate click event to inner view.
     */
    override fun setOnClickListener(l: OnClickListener?) {
        mBackgroundImageView.setOnClickListener(l)
    }

    companion object {
        private const val PENDING = 0
        private const val LOADING = 1
    }
}