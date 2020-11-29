package com.nemesiss.dev.piaprobox.Model.User

import com.alibaba.fastjson.annotation.JSONField

data class LoginCookie(
    @JSONField(name = "piapro_s")
    val piapro_s: String,
    @JSONField(name = "piapro_r")
    val piapro_r: String
)