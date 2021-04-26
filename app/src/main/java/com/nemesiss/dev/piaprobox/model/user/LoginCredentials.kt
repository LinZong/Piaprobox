package com.nemesiss.dev.piaprobox.model.user

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.annotation.JSONField

/**
 * 最简单的LoginCredentials，包含用户名和密码
 * 这个可以作为基类，拓展出其他复杂的Credentials
 * 所以这个类给open
 */
open class LoginCredentials(
    @JSONField(name = "_username") val userName: String,
    @JSONField(name = "_password") val password: String
) {
    val json: String
        @JSONField(serialize = false)
        get() = JSON.toJSONString(this)

    val queryString: String
        @JSONField(serialize = false)
        get() = "_username=${userName}&_password=${password}&_remember_me=on&login=ログイン"
}