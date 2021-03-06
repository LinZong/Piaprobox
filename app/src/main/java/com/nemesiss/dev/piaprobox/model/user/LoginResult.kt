package com.nemesiss.dev.piaprobox.model.user

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
    NETWORK_ERR,
    /**
     * 成功
     */
    SUCCESS,
    /**
     * 未知错误
     */
    UNKNOWN_ERR,

    /**
     * 被用户取消
     */
    CANCELED_BY_USER
}