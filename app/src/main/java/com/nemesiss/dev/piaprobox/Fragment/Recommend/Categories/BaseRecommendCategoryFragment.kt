package com.nemesiss.dev.piaprobox.Fragment.Recommend.Categories

import com.nemesiss.dev.piaprobox.Fragment.Main.BaseMainFragment
import com.nemesiss.dev.piaprobox.Fragment.Recommend.RecommendListType

abstract class BaseRecommendCategoryFragment : BaseMainFragment() {

    abstract override fun Refresh()

    override fun LoadBannerImage() {

    }

    abstract fun LoadDefaultPage(contentType: RecommendListType, ShouldUpdateTagList : Boolean = true)

    abstract fun LoadRecommendList(tagUrl : String, contentType: RecommendListType)

    abstract fun OnTagItemSelected(index : Int)

    abstract fun OnRecommendItemSelected(index: Int)

    abstract fun ParseTagListContent(HTMLString: String)

    abstract fun ParseRecommendListContent(HTMLString: String, contentType: RecommendListType)

}