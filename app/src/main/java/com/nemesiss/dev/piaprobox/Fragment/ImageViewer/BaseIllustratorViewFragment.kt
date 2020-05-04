package com.nemesiss.dev.piaprobox.Fragment.ImageViewer

import com.nemesiss.dev.HTMLContentParser.Model.RelatedImageInfo
import com.nemesiss.dev.piaprobox.Fragment.Main.BaseMainFragment

abstract class BaseIllustratorViewFragment : BaseMainFragment()
{
    abstract fun OnRelatedItemSelected(index : Int, relatedImageInfo: RelatedImageInfo)
}