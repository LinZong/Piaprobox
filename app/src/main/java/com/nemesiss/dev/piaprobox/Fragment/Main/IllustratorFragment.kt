package com.nemesiss.dev.piaprobox.Fragment.Main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nemesiss.dev.piaprobox.R
import kotlinx.android.synthetic.main.fragment_header.*

class IllustratorFragment : BaseMainFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.illustrator_fragment, container, false)
    }

    override fun Refresh() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun LoadBannerImage() {
        BaseMainFragment_Banner_ImageView.setImageResource(R.drawable.illu_banner)
    }
}