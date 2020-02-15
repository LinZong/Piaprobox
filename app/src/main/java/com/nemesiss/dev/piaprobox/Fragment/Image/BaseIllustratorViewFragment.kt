package com.nemesiss.dev.piaprobox.Fragment.Image

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nemesiss.dev.HTMLContentParser.Model.RelatedImageInfo
import com.nemesiss.dev.piaprobox.Fragment.Main.BaseMainFragment

abstract class BaseIllustratorViewFragment : BaseMainFragment()
{
    abstract fun OnRelatedItemSelected(index : Int, relatedImageInfo: RelatedImageInfo)
}