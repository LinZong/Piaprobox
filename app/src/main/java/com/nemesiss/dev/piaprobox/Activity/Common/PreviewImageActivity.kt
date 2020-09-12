package com.nemesiss.dev.piaprobox.Activity.Common

import android.graphics.drawable.Drawable
import android.os.Bundle
import com.nemesiss.dev.piaprobox.Fragment.ImageViewer.IllustratorViewFragment
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.View.Common.wrapDragAndCloseTouchHandler
import kotlinx.android.synthetic.main.activity_preview_image.*

class PreviewImageActivity : PiaproboxBaseActivity() {
    companion object {
        @JvmStatic
        private var PRE_SHOWN_DRAWABLE: Drawable? = null
            get() {
                val prev = field
                field = null
                return prev
            }

        @JvmStatic
        @Synchronized
        fun SetPreShownDrawable(drawable: Drawable) {
            PRE_SHOWN_DRAWABLE = drawable
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview_image)
        Preview_Pinch_ImageView.setImageDrawable(PRE_SHOWN_DRAWABLE)
        Preview_Pinch_ImageView.transitionName = resources.getString(R.string.ImageViewTransitionName)
        Preview_Image_BackButton.setOnClickListener { exit() }
        wrapDragAndCloseTouchHandler(Preview_Pinch_ImageView, Preview_Image_Background, 200)
    }

    private fun exit() {
        setResult(IllustratorViewFragment.PREVIEW_IMAGE_RES_CODE)
        supportFinishAfterTransition()
    }

    override fun onBackPressed() {
        exit()
    }
}