package com.nemesiss.dev.piaprobox.Adapter.RecommendPage

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.nemesiss.dev.piaprobox.Fragment.HomePage.Recommend.Categories.BaseRecommendCategoryFragment

class RecommendCategoryFragmentPageAdapter(fm: FragmentManager,var fragments : List<BaseRecommendCategoryFragment>) : FragmentPagerAdapter(fm) {
    override fun getItem(p0: Int): Fragment {
        return fragments[p0]
    }

    override fun getCount(): Int {
        return fragments.size
    }
}