package com.nemesiss.dev.piaprobox.Model.Resources

import android.graphics.Color
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

enum class CryptonCharacterColorDefinition(
    val ColorInt: Int
) {
    HATSUNE_MIKU(Color.parseColor("#72e7e9")),
    KAGAMINE_RIN(Color.parseColor("#eee96e")),
    KAGAMINE_REN(Color.parseColor("#ffd200")),
    MEGURINE_LUKA(Color.parseColor("#fc7de6")),
    KAITO(Color.parseColor("#5172ec")),
    MEIKO(Color.parseColor("#ff3939"))
}