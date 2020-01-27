package com.nemesiss.dev.piaprobox.Fragment.Main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View

abstract class BaseMainFragment: Fragment() {
    abstract fun Refresh()
    abstract fun LoadBannerImage()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LoadBannerImage()
    }
}