package com.nemesiss.dev.piaprobox.Adapter.RecommendPage

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.nemesiss.dev.piaprobox.Fragment.Recommend.BaseRecommendFragment

class RecommendCategoryFragmentPageAdapter(fm: FragmentManager,var fragments : List<BaseRecommendFragment>) : FragmentPagerAdapter(fm) {
    override fun getItem(p0: Int): Fragment {
        return fragments[p0]
    }

    override fun getCount(): Int {
        return fragments.size
    }
}