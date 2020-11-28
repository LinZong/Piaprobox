package com.nemesiss.dev.piaprobox.Model.User

/**
 * 登陆调用结果
 */
enum class LoginResult {
    /**
     * 用户名或密码错误
     */
    ACCOUNT_OR_PASSWORD_WRONG,

    /**
     * 网络出错
     */
    NETWORK_ERR
}