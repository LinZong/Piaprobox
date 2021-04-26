package com.nemesiss.dev.piaprobox.Activity.Common

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Toast
import com.nemesiss.dev.piaprobox.Service.Player.MusicPlayerService
import com.nemesiss.dev.piaprobox.view.common.LoadingIndicatorView

open class PiaproboxBaseActivity : AppCompatActivity() {

    companion object {
        @JvmStatic
        var activities = ArrayList<PiaproboxBaseActivity>(0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activities.add(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        activities.removeAt(activities.size - 1)
        if (activities.isEmpty() && !MusicPlayerService.IS_FOREGROUND) {
            stopService(Intent(this, MusicPlayerService::class.java))
        }
    }

    protected var loadingIndicatorView: LoadingIndicatorView? = null

    protected fun ShowLoadingIndicator(rootViewGroup: ViewGroup) {
        runOnUiThread {
            val topView = rootViewGroup
            loadingIndicatorView = LoadingIndicatorView(this)
            topView.addView(loadingIndicatorView)
        }
    }

    protected fun HideLoadingIndicator(rootViewGroup: ViewGroup) {
        runOnUiThread {
            val topView = rootViewGroup
            if (loadingIndicatorView != null) {
                topView.removeView(loadingIndicatorView)
            }
            loadingIndicatorView = null
        }
    }

    fun LoadFailedTips(code: Int, message: String) {
        runOnUiThread {
            showToastHint("$message ($code)")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {
            android.R.id.home -> {
                finish()
            }
        }

        return true
    }

    fun ShowToolbarBackIcon(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    fun showToastHint(hintText: String) {
        runOnUiThread { Toast.makeText(this, hintText, Toast.LENGTH_SHORT).show() }
    }
}