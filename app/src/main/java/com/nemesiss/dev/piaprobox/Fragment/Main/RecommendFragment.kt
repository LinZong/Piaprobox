package com.nemesiss.dev.piaprobox.Fragment.Main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nemesiss.dev.piaprobox.Application.PiaproboxApplication
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.HTMLParser
import com.nemesiss.dev.piaprobox.View.Common.LoadingIndicatorView
import kotlinx.android.synthetic.main.fragment_header.*

class RecommendFragment : BaseMainFragment() {

    private lateinit var htmlParser : HTMLParser

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.recommand_fragment, container, false)
    }

    override fun Refresh() {
        val topView = activity?.findViewById<ViewGroup>(android.R.id.content)
        topView?.addView(LoadingIndicatorView(context))
    }

    override fun LoadBannerImage() {
        Log.d("RecommendFragment", BaseMainFragment_Banner_ImageView.toString())
        BaseMainFragment_Banner_ImageView.setImageResource(R.drawable.recommand_banner)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        htmlParser = HTMLParser(context ?: PiaproboxApplication.Self.applicationContext)
    }

    private fun LoadContent() {

    }
}