package com.nemesiss.dev.piaprobox.Service.User

import com.nemesiss.dev.piaprobox.Activity.Common.LoginCallbackActivity
import com.nemesiss.dev.piaprobox.Model.User.LoginCredentials
import com.nemesiss.dev.piaprobox.Model.User.LoginResult
import com.nemesiss.dev.piaprobox.Model.User.LoginStatus
import com.nemesiss.dev.piaprobox.Model.User.UserInfo

/**
 * This service is used to maintain user login status,
 * such as login callback (ensure user is in login status or redirect to LoginActivity for a further login action)
 * inject and persist user cookie into SharedPreferences, etc.
 */

typealias LoginCallback = (UserInfo) -> Unit

class NotLoginException : Exception()

class NoLoginCredentialsException : Exception()

open class LoginFailedException @JvmOverloads constructor(
    val loginResult: LoginResult,
    message: String? = null,
    cause: Throwable? = null,
    enableSuppression: Boolean = true,
    writableStackTrace: Boolean = true
) :
    Exception(message, cause, enableSuppression, writableStackTrace)

class GetUserInfoFailedException @JvmOverloads constructor(
    loginResult: LoginResult,
    message: String? = null,
    cause: Throwable? = null,
    enableSuppression: Boolean = true,
    writableStackTrace: Boolean = true
) : LoginFailedException(loginResult, message, cause, enableSuppression, writableStackTrace)

interface UserLoginService {
    /**
     * 获取用户信息。未登录会抛出异常
     */
    @Throws(NotLoginException::class)
    fun getUserInfo(): UserInfo

    /**
     * 获取登陆信息，如果没存储过会返回null
     */
    fun getLoginCredentials(): LoginCredentials?

    /**
     * 保存登陆信息
     * @return 是否保存成功
     */
    fun saveLoginCredentials(credentials: LoginCredentials): Boolean

    /**
     * 使用保存的Credentials登陆，如果没有保存过登录信息，会抛出异常
     * 这是同步调用，登陆成功会返回UserInfo，不成功则会抛出登陆失败异常
     */
    @Throws(NoLoginCredentialsException::class)
    fun login(): UserInfo

    /**
     * 使用传入的Credentials登陆，这会覆盖掉当前保存的登陆信息。
     * 这是同步调用，登陆成功会返回UserInfo，不成功则会抛出登陆失败异常
     */
    @Throws(LoginFailedException::class, GetUserInfoFailedException::class)
    fun login(credentials: LoginCredentials): UserInfo

    /**
     * 登出用户:
     * 如果当前登录状态有效，则会进行登出动作
     * 否则什么也不会发生。
     */
    fun logout()

    /**
     * 跳转至LoginActivity进行登陆。
     */
    fun startLoginActivity(loginCallbackActivity: LoginCallbackActivity)

    /**
     * 检测当前用户的登录态是否有效。
     * 因为调用完整走一遍检测函数，需要请求Piapro官网，耗时较长，默认会去读缓存，缓存有效期30分钟（在SP里）
     * 也可以手动指定不读缓存，强制走检测函数。
     * 只要完整的走了一遍检测函数后，检测结果将会写入缓存。
     */
    fun checkLogin(useCache: Boolean = true): LoginStatus
}