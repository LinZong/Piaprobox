package com.nemesiss.dev.piaprobox.view.common

import android.annotation.SuppressLint
import android.graphics.Outline
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.os.Build
import android.view.View
import android.view.ViewOutlineProvider
import org.slf4j.LoggerFactory

@SuppressLint("PrivateApi")
object ShapeOutlineUtil {

    private val log = LoggerFactory.getLogger(ShapeOutlineUtil::class.java)

    private var mGradientStateClazz: Class<*>? = null

    init {
        try {
            mGradientStateClazz = Class.forName("android.graphics.drawable.GradientDrawable\$GradientState")
        } catch (e: Exception) {
            log.error("Cannot get the class instance of android.graphics.drawable.GradientDrawable.GradientState ", e)
        }
    }

    fun handleLayeredDrawable(rippleDrawable: RippleDrawable): ViewOutlineProvider {
        val layers = rippleDrawable.numberOfLayers
        for (i in 0 until layers) {
            val resolvedProvider = when (val childDrawable = rippleDrawable.getDrawable(i)) {
                is ShapeDrawable -> handleShapedDrawable(childDrawable)
                is GradientDrawable -> handleShapedDrawable(childDrawable)
                else -> null
            }
            return resolvedProvider ?: continue
        }
        return ViewOutlineProvider.BACKGROUND
    }

    fun handleShapedDrawable(shapeDrawable: ShapeDrawable): ViewOutlineProvider {
        val shapedOutline = Outline()
        shapeDrawable.shape.getOutline(shapedOutline)
        return object : ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: Outline?) {
                outline?.set(shapedOutline)
            }
        }
    }

    fun handleShapedDrawable(gradientDrawable: GradientDrawable): ViewOutlineProvider {
        val shapedOutline = Outline()
        gradientDrawable.invokeEnsureValidRect()
        when (getGradientDrawableShape(gradientDrawable)) {
            GradientDrawable.LINE -> {
                // mRect
                val mRect = gradientDrawable.getRect("mRect")
                shapedOutline.setRect(mRect.left.toInt(), mRect.top.toInt(), mRect.right.toInt(), mRect.bottom.toInt())
            }
            GradientDrawable.OVAL -> {
                // oval mRect
                val mRect = gradientDrawable.getRect("mRect")
                shapedOutline.setOval(mRect.left.toInt(), mRect.top.toInt(), mRect.right.toInt(), mRect.bottom.toInt())
            }
            else -> {
                // Get the shape of GradientDrawable failed.
                // Fall back to ViewOutlineProvider.BACKGROUND
                return ViewOutlineProvider.BACKGROUND
            }
        }
        return object : ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: Outline?) {
                outline?.set(shapedOutline)
            }
        }
    }

    @SuppressLint("SoonBlockedPrivateApi")
    private fun GradientDrawable.invokeEnsureValidRect() {
        val method = GradientDrawable::class.java.getDeclaredMethod("ensureValidRect")
        method.isAccessible = true
        method.invoke(this)
    }

    private fun GradientDrawable.getRect(fieldName: String): RectF {
        return getField(fieldName)
    }

    private inline fun <reified T> GradientDrawable.getField(fieldName: String): T {
        val pathField = GradientDrawable::class.java.getDeclaredField(fieldName)
        pathField.isAccessible = true
        return pathField.get(this) as T
    }

    private fun getGradientDrawableShape(gradientDrawable: GradientDrawable): Int {
        // if running device is Android 7.0 and above, we can
        // feel free to get gradient shape by public getter.
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            return gradientDrawable.shape
        }
        return mGradientStateClazz?.let { gradientStateClazz ->
            // or we can only do some tricky reflection to achieve this.
            try {
                val mGradientState = gradientDrawable.getField<Any>("mGradientState")
                val mShapeField = gradientStateClazz.getDeclaredField("mShape")
                mShapeField.isAccessible = true
                val mShape = mShapeField.get(mGradientState)
                return mShape as Int
            } catch (e: Exception) {
                log.error("getGradientDrawableShape failed. ", e)
            }
            return -1
        } ?: -1
    }
}