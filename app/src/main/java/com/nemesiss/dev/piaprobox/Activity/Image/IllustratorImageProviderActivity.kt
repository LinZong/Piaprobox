package com.nemesiss.dev.piaprobox.Activity.Image

import android.graphics.drawable.Drawable
import com.nemesiss.dev.contentparser.model.RecommendItemModelImage
import com.nemesiss.dev.contentparser.model.RelatedImageInfo
import com.nemesiss.dev.piaprobox.Activity.Common.PiaproboxBaseActivity
import com.nemesiss.dev.piaprobox.Fragment.ImageViewer.IllustratorViewFragment

abstract class IllustratorImageProviderActivity : PiaproboxBaseActivity() {

    companion object {
        @JvmStatic
        var CAN_VIEW_ITEM_LIST: List<RecommendItemModelImage>? = null
            protected set

        @JvmStatic
        var PRE_SHOWN_IMAGE: Drawable? = null
            protected set

        @JvmStatic
        @Synchronized
        fun SetItemList(list: List<RecommendItemModelImage>) {
            CAN_VIEW_ITEM_LIST = list
        }
        @JvmStatic
        @Synchronized
        fun SetPreShownDrawable(drawable: Drawable?) {
            PRE_SHOWN_IMAGE = drawable
        }
        @JvmStatic
        @Synchronized
        fun CleanUp() {
            // Set null to help gc.
            CAN_VIEW_ITEM_LIST = null
            PRE_SHOWN_IMAGE = null
        }
    }

    override fun onDestroy() {
        CleanUp()
        super.onDestroy()
    }


    abstract fun AskForViewModel(fragmentIndex: Int, self: IllustratorViewFragment)

    abstract fun AskForViewModel(fragmentIndex: Int, relatedImageInfo: RelatedImageInfo)

    abstract fun HandleDownloadImage(ImageURL: String, Title: String)

    abstract fun HandleClose()
}