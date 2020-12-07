package com.nemesiss.dev.piaprobox.View.Common

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.support.annotation.ColorInt
import android.support.annotation.Dimension
import android.support.annotation.Dimension.PX
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Util.AppUtil
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
                when (getInt(R.styleable.LoadingButton_initMode, 0)) {
                    PENDING -> pending()
                    LOADING -> loading()
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
        mIconContainer.removeAllViews()
        mIconContainer.addView(lazyPendingIcon.value)

    }

    fun loading() {
        mIconContainer.removeAllViews()
        mIconContainer.addView(lazyLoadingIcon.value)
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