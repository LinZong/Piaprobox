package com.nemesiss.dev.piaprobox.Activity.Common

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.AsyncExecutor
import com.nemesiss.dev.piaprobox.util.AnimatorListenerBuilder
import com.nemesiss.dev.piaprobox.util.AppUtil
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : PiaproboxBaseActivity() {

    var asyncExecutor: AsyncExecutor = AsyncExecutor.INSTANCE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        AppUtil.HideNavigationBar(this)
        FadeInPiaproIcon()
    }


    private fun JumpToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun FadeInPiaproIcon() {
        val animator = ObjectAnimator.ofFloat(Splash_Piapro_Icon_Image, "alpha", 0f, 1f)
        animator.duration = 300
        animator.addListener(
            AnimatorListenerBuilder()
                .End {
                    asyncExecutor.SendTaskMainThreadDelay(
                        Runnable {
                            JumpToMainActivity()
                            finish()
                        }
                        , 500)
                }.Build()
        )
        animator.start()
    }
}
