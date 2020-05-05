package com.nemesiss.dev.piaprobox.Fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.nemesiss.dev.piaprobox.Application.PiaproboxApplication
import com.nemesiss.dev.piaprobox.View.Common.LoadingIndicatorView

abstract class BaseMainFragment : Fragment() {
    open fun Refresh() {

    }
    open fun LoadBannerImage() {

    }

    protected var loadingIndicatorView : LoadingIndicatorView? = null

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
        activity?.runOnUiThread {
            val topView = view as ViewGroup
            loadingIndicatorView = LoadingIndicatorView(context)
            topView.addView(loadingIndicatorView)
        }
    }

    fun HideLoadingIndicator() {
        activity?.runOnUiThread {
            val topView = view as ViewGroup
            if (loadingIndicatorView != null) {
                topView.removeView(loadingIndicatorView)
            }
            loadingIndicatorView = null
        }
    }
}