package com.nemesiss.dev.piaprobox.Misc

import com.nemesiss.dev.piaprobox.R

class StaticResourcesMap {
    companion object {
        @JvmStatic
        val DefaultThumbMaps =
            arrayOf(
                "th-empty",
                "th-miku",
                "th-meiko",
                "th-rin",
                "th-kaito",
                "th-luka"
            ).zip(
                arrayOf(
                    R.drawable.thumb_empty,
                    R.drawable.thumb_miku,
                    R.drawable.thumb_meiko,
                    R.drawable.thumb_rin,
                    R.drawable.thumb_kaito,
                    R.drawable.thumb_luka
                )
            )
                .toMap()

    }
}

enum class RecyclerViewInnerIndicator(val TAG: String, val FLAG: Int) {
    RECYCLER_VIEW_LOAD_MORE_INDICATOR("RECYCLER_VIEW_LOAD_MORE_INDICATOR", 1),
    RECYCLER_VIEW_NOTHING_MORE_INDICATOR("RECYCLER_VIEW_NOTHING_MORE_INDICATOR", 2);
}