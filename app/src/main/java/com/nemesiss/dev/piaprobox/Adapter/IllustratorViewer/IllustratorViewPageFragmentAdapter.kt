package com.nemesiss.dev.piaprobox.Adapter.IllustratorViewer

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.nemesiss.dev.piaprobox.Fragment.ImageViewer.BaseIllustratorViewFragment

class IllustratorViewPageFragmentAdapter(var fragments: List<BaseIllustratorViewFragment>, fm: FragmentManager) :
    FragmentPagerAdapter(
        fm
    ) {
    override fun getItem(p0: Int): Fragment {
        return fragments[p0]
    }

    override fun getCount(): Int {
        return fragments.size
    }
}