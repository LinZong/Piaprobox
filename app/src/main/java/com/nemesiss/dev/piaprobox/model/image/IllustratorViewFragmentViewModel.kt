package com.nemesiss.dev.piaprobox.model.image

import android.graphics.drawable.Drawable
import com.nemesiss.dev.contentparser.model.RelatedImageInfo

class IllustratorViewFragmentViewModel {
    lateinit var BrowserPageUrl : String
    lateinit var ArtistName : String
    lateinit var ArtistAvatarUrl : String
    lateinit var Title : String
    lateinit var CreateDescription : String
    lateinit var CreateDetailRaw : String
    lateinit var ItemImageUrl : String
    var ItemImageThumbDrawable : Drawable? = null
    lateinit var RelatedItems : List<RelatedImageInfo>
}

