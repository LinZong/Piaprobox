package com.nemesiss.dev.piaprobox.Service.User

import android.content.Intent
import com.nemesiss.dev.piaprobox.Activity.Common.LoginActivity
import com.nemesiss.dev.piaprobox.Activity.Common.LoginCallbackActivity
import com.nemesiss.dev.piaprobox.Model.Resources.Constants
import com.nemesiss.dev.piaprobox.Model.User.LoginCredentials
import com.nemesiss.dev.piaprobox.Model.User.LoginStatus
import com.nemesiss.dev.piaprobox.Model.User.UserInfo
import com.nemesiss.dev.piaprobox.Service.Persistence
import okhttp3.OkHttpClient
import java.util.*
import javax.inject.Inject

/**
 * Maintain Piapro's cookie for a login status.
 * This is a simple fashion also being used for Piapro's official site.
 */
class CookieUserLoginServiceImpl @Inject constructor(val httpClient: OkHttpClient) : UserLoginService {
    override fun getUserInfo(): UserInfo = Persistence.GetUserInfo() ?: throw NotLoginException()

    private fun saveUserInfo(userInfo: UserInfo): Boolean {
        return Persistence.SaveUserInfo(userInfo)
    }

    override fun getLoginCredentials(): LoginCredentials? {
        return Persistence.GetLoginCredentials()
    }

    override fun saveLoginCredentials(credentials: LoginCredentials): Boolean {
        return Persistence.SaveLoginCredentials(credentials)
    }

    override fun login(): UserInfo {
        TODO("Not implemented")
    }

    override fun login(credentials: LoginCredentials): UserInfo {
        TODO("Not implemented")
    }

    override fun startLoginActivity(loginCallbackActivity: LoginCallbackActivity) {
        loginCallbackActivity.startActivityForResult(
            Intent(loginCallbackActivity, LoginActivity::class.java),
            Constants.Login.REQUEST_CODE
        )
    }

    override fun checkLogin(useCache: Boolean): LoginStatus {
        if (useCache) {
            // 如果loginTimeStamp == 0 或者当前时间已经超过Cache有效期，则尝试强制发出网络请求，查询登录态
            if (checkCachedLoginStatusValid()) {
                return LoginStatus.LOGIN
            }
        }
        return forceCheckLogin()
    }

    private fun checkCachedLoginStatusValid(): Boolean {
        // in milliseconds
        val now = Date().time
        // in milliseconds
        val loginTimeStamp = Persistence.GetLoginTimeStamp()
        // second to milliseconds
        val cacheStillValidInterval = Constants.Login.LOGIN_CACHE_VALID_TIME_INTERVAL_SEC * 1000
        return loginTimeStamp > 0 && (now - loginTimeStamp) < cacheStillValidInterval
    }

    private fun forceCheckLogin(): LoginStatus {
        return LoginStatus.NOT_LOGIN
    }
}