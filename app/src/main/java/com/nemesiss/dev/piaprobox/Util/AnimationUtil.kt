package com.nemesiss.dev.piaprobox.Util

import android.animation.Animator

class AnimationUtil {
}

class AnimatorListenerBuilder {
    private var AnimatorEnd : ((Animator?)->Unit)? = null
    private var AnimatorRepeat : ((Animator?)->Unit)? = null
    private var AnimatorStart : ((Animator?)->Unit)? = null
    private var AnimatorCancel : ((Animator?)->Unit)? = null

    fun Start(Handler : ((Animator?)->Unit)?) : AnimatorListenerBuilder {
        AnimatorStart = Handler
        return this
    }
    fun End(Handler : ((Animator?)->Unit)?) : AnimatorListenerBuilder {
        AnimatorEnd = Handler
        return this
    }
    fun Repeat(Handler : ((Animator?)->Unit)?) : AnimatorListenerBuilder {
        AnimatorRepeat = Handler
        return this
    }
    fun Cancel(Handler : ((Animator?)->Unit)?) : AnimatorListenerBuilder {
        AnimatorCancel = Handler
        return this
    }

    fun Build() : Animator.AnimatorListener {
        return object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {
                AnimatorRepeat?.invoke(p0)
            }

            override fun onAnimationEnd(p0: Animator?) {
                AnimatorEnd?.invoke(p0)
            }

            override fun onAnimationCancel(p0: Animator?) {
                AnimatorCancel?.invoke(p0)
            }

            override fun onAnimationStart(p0: Animator?) {
                AnimatorStart?.invoke(p0)
            }
        }
    }
}
