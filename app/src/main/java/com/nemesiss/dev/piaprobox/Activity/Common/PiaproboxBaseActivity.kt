package com.nemesiss.dev.piaprobox.Activity.Common

import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Toast
import com.nemesiss.dev.piaprobox.Application.PiaproboxApplication
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.View.Common.LoadingIndicatorView

open class PiaproboxBaseActivity : AppCompatActivity() {

    protected var loadingIndicatorView : LoadingIndicatorView? = null

    protected fun ShowLoadingIndicator(rootViewGroup : ViewGroup) {
        runOnUiThread {
            val topView = rootViewGroup
            loadingIndicatorView = LoadingIndicatorView(this)
            topView.addView(loadingIndicatorView)
        }
    }

    protected fun HideLoadingIndicator(rootViewGroup : ViewGroup) {
        runOnUiThread {
            val topView = rootViewGroup
            if (loadingIndicatorView != null) {
                topView.removeView(loadingIndicatorView)
            }
            loadingIndicatorView = null
        }
    }
    fun LoadFailedTips(code: Int, message: String) {
        Toast.makeText(
            this,
            "$message ($code)",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when(item?.itemId) {
            android.R.id.home -> {
                finish()
            }
        }

        return true
    }
}