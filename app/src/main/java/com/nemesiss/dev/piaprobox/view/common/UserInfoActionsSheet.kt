package com.nemesiss.dev.piaprobox.view.common

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.design.widget.NavigationView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.nemesiss.dev.piaprobox.R

class UserInfoActionsSheet : BottomSheetDialogFragment() {

    private var actionsNavigationView: NavigationView? = null

    private var sheetItemSelectedListener: NavigationView.OnNavigationItemSelectedListener? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.userinfo_modal_options_layout, container, false).apply {
            actionsNavigationView = findViewById(R.id.pick_userinfo_action_navigation)
            if (sheetItemSelectedListener != null) {
                actionsNavigationView?.setNavigationItemSelectedListener(sheetItemSelectedListener)
            }
        }
    }

    fun setOnItemClickedListener(listener: NavigationView.OnNavigationItemSelectedListener) {
        sheetItemSelectedListener = listener
        actionsNavigationView?.setNavigationItemSelectedListener(sheetItemSelectedListener)
    }

    fun setOnItemClickedListener(listener: (MenuItem) -> Boolean) {
        sheetItemSelectedListener = NavigationView.OnNavigationItemSelectedListener(listener)
        actionsNavigationView?.setNavigationItemSelectedListener(sheetItemSelectedListener)
    }
}