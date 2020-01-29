package com.nemesiss.dev.piaprobox.Activity

import android.os.Bundle
import android.os.Handler
import com.nemesiss.dev.piaprobox.Activity.Common.PiaproboxBaseActivity
import com.nemesiss.dev.piaprobox.R
import kotlinx.android.synthetic.main.activity_test_skeleton.*

class TestSkeletonActivity : PiaproboxBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_skeleton)


        Handler().postDelayed(Runnable {
            skeletonGroup.finishAnimation()
        },3000)
    }



}