package com.nemesiss.dev.piaprobox.Fragment.ImageViewer

import com.nemesiss.dev.contentparser.model.RelatedImageInfo
import com.nemesiss.dev.piaprobox.Fragment.BaseMainFragment

abstract class BaseIllustratorViewFragment : BaseMainFragment()
{
    abstract fun OnRelatedItemSelected(index : Int, relatedImageInfo: RelatedImageInfo)
}