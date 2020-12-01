package com.nemesiss.dev.piaprobox.Model.User

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.annotation.JSONField

/**
 * 最简单的LoginCredentials，包含用户名和密码
 * 这个可以作为基类，拓展出其他复杂的Credentials
 * 所以这个类给open
 */
open class LoginCredentials(
    @JSONField(name = "_username") val UserName: String,
    @JSONField(name = "_password") val Password: String
) {
    val json get() = JSON.toJSONString(this)
}