package com.nemesiss.dev.piaprobox.Model.User

import android.text.TextUtils
import com.alibaba.fastjson.annotation.JSONField

data class LoginCookie(
    @JSONField(name = "piapro_s")
    val piapro_s: String,
    @JSONField(name = "piapro_r")
    val piapro_r: String ,
    val expires: Long
) {
    val pairs
        get() = arrayOf(
            "piapro_s" to piapro_s,
            "piapro_r" to piapro_r
        )

    val isValid get() = !TextUtils.isEmpty(piapro_s) && !TextUtils.isEmpty(piapro_r)
}