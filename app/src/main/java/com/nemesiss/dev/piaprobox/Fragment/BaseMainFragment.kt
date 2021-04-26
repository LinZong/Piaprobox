package com.nemesiss.dev.piaprobox.Fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import com.nemesiss.dev.piaprobox.Activity.Common.MainActivity
import com.nemesiss.dev.piaprobox.Application.PiaproboxApplication
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.view.common.LoadingIndicatorView

abstract class BaseMainFragment : Fragment() {
    open fun Refresh() {

    }
    open fun LoadBannerImage() {

    }

    private var loadingIndicatorView : LoadingIndicatorView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LoadBannerImage()
    }


    fun LoadFailedTips(code: Int, message: String) {
        activity?.runOnUiThread {
            Toast.makeText(
                context ?: PiaproboxApplication.Self.applicationContext,
                "$message ($code)",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    fun ShowLoadingIndicator() {
        (activity as? MainActivity)?.DisableRefreshButton = true
        activity?.runOnUiThread {
            var topView = view as ViewGroup
            if(topView is LinearLayout) {
                topView = topView.findViewById(R.id.Category_LoadingIndicatorMaskViewGroupRoot)
            }
            loadingIndicatorView = LoadingIndicatorView(context)
            topView.addView(loadingIndicatorView)
        }
    }

    fun HideLoadingIndicator() {
        (activity as? MainActivity)?.DisableRefreshButton = false
        activity?.runOnUiThread {
            var topView = view as ViewGroup
            if(topView is LinearLayout) {
                topView = topView.findViewById(R.id.Category_LoadingIndicatorMaskViewGroupRoot)
            }
            if (loadingIndicatorView != null) {
                topView.removeView(loadingIndicatorView)
            }
            loadingIndicatorView = null
        }
    }
}