package com.nemesiss.dev.piaprobox.Fragment.Main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import android.widget.Toast
import com.nemesiss.dev.piaprobox.Application.PiaproboxApplication

abstract class BaseMainFragment : Fragment() {
    abstract fun Refresh()
    abstract fun LoadBannerImage()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LoadBannerImage()
    }


    fun LoadFailedTips(code: Int, message: String) {
        Toast.makeText(
            context ?: PiaproboxApplication.Self.applicationContext,
            "$message ($code)",
            Toast.LENGTH_SHORT
        ).show()
    }

}