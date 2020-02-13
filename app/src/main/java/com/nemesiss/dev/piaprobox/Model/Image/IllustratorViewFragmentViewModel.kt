package com.nemesiss.dev.piaprobox.Model.Image

import android.graphics.drawable.Drawable
import com.nemesiss.dev.HTMLContentParser.Model.RelatedImageInfo

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

