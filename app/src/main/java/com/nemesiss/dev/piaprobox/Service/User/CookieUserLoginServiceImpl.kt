package com.nemesiss.dev.piaprobox.Service.User

import android.content.Intent
import android.text.TextUtils
import com.nemesiss.dev.piaprobox.Activity.Common.LoginActivity
import com.nemesiss.dev.piaprobox.Activity.Common.LoginCallbackActivity
import com.nemesiss.dev.piaprobox.Model.Resources.Constants
import com.nemesiss.dev.piaprobox.Model.User.*
import com.nemesiss.dev.piaprobox.Service.AsyncExecutor
import com.nemesiss.dev.piaprobox.Service.HTMLParser
import com.nemesiss.dev.piaprobox.Service.Persistence
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.DaggerFetchFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.*
import javax.inject.Inject


/**
 * Maintain Piapro's cookie for a login status.
 * This is a simple fashion also being used for Piapro's official site.
 */
class CookieUserLoginServiceImpl @Inject constructor(val httpClient: OkHttpClient, val htmlParser: HTMLParser) :
    UserLoginService {


    private val asyncExecutor = AsyncExecutor.INSTANCE

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
        try {
            val loginCredentials = Persistence.GetLoginCredentials()
                ?: throw NoLoginCredentialsException()

            /**
             * 登陆分两步。
             * 1. 访问piapro.jp官网，拿到piapro_s的Cookie
             * 2. 带着piapro_s的Cookie去发用户名密码
             * 3. 检测是否登陆成功（看服务器有没有下发piapro_r)
             * 4. 如果成功，保存Cookie, 并获取UserInfo
             */

            // 1
            val piapro_s = getPiapro_s()
            // 2
            val piapro_r = getPiapro_r(piapro_s, loginCredentials)

            val loginCookie = LoginCookie(piapro_s, piapro_r)

            Persistence.SaveLoginCookie(loginCookie)
            Persistence.SaveLoginStatus(LoginStatus.LOGIN)


        } catch (e: Exception) {
            Persistence.SaveLoginStatus(LoginStatus.NOT_LOGIN)
            throw e
        }
        TODO("还没写完2")
    }

    override fun login(credentials: LoginCredentials): UserInfo {
        Persistence.SaveLoginCredentials(credentials)
        // Now we can call login method without parameters.
        return login()
    }

    private fun getUserInfoFromPiapro(): UserInfo {
        // When we reach here we must have a correct login credentials.
        val loginCredentials = Persistence.GetLoginCredentials()!!
        val loginCookie = Persistence.GetLoginCookie()!!
        val userProfileUrl = Constants.Url.getUserProfileUrl(loginCredentials.UserName)

        try {
            val response = DaggerFetchFactory.create().fetcher().withLoginCookie().visit(userProfileUrl).go()
            if (response.isSuccessful) {
                val html = response.body?.string()
                
            }
        } catch (ioe: IOException) {
        } catch (e: Exception) {

        }
        TODO("还没写完")
    }


    private fun getPiapro_r(piapro_s_Cookie: String, loginCredentials: LoginCredentials): String {
        val mediaType = "application/x-www-form-urlencoded".toMediaType()
        val body: RequestBody = loginCredentials.json.toRequestBody(mediaType)
        val request: Request = Request.Builder()
            .url("https://piapro.jp/login/exe")
            .method("POST", body)
            .addHeader("Connection", "keep-alive")
            .addHeader("Cache-Control", "max-age=0")
            .addHeader("sec-ch-ua", "\"\\Not;A\"Brand\";v=\"99\", \"Google Chrome\";v=\"85\", \"Chromium\";v=\"85\"")
            .addHeader("sec-ch-ua-mobile", "?0")
            .addHeader("Upgrade-Insecure-Requests", "1")
            .addHeader("Origin", "https://piapro.jp")
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.83 Safari/537.36"
            )
            .addHeader(
                "Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"
            )
            .addHeader("Sec-Fetch-Site", "same-origin")
            .addHeader("Sec-Fetch-Mode", "navigate")
            .addHeader("Sec-Fetch-User", "?1")
            .addHeader("Sec-Fetch-Dest", "document")
            .addHeader("Referer", "https://piapro.jp/login/")
            .addHeader("Accept-Language", "en-US,en;q=0.9")
            .addHeader("Cookie", "piapro_s=${piapro_s_Cookie}")
            .build()

        try {
            val response = httpClient.newCall(request).execute()
            val piapro_r = response.header("piapro_r", "")!!
            if (TextUtils.isEmpty(piapro_r)) {
                throw LoginFailedException(LoginResult.ACCOUNT_OR_PASSWORD_WRONG)
            }
            return piapro_r
        } catch (ioe: IOException) {
            throw LoginFailedException(
                LoginResult.NETWORK_ERR,
                "Cannot get the 'piapro_r' cookie due to network error."
            )
        } catch (e: Exception) {
            throw LoginFailedException(
                LoginResult.NETWORK_ERR,
                "Cannot get the 'piapro_r' cookie due to unknown exception."
            )
        }
    }

    private fun getPiapro_s(): String {
        // 之所以选用LoginPage, 是因为这个页面的大小只有首页的1/4，但是同样能具有拿到piapro_s的能力。
        val request = Request.Builder().url(Constants.Url.LOGIN_PAGE).get().build()
        try {
            val response = httpClient.newCall(request).execute()
            val piapro_s_Cookie = response.header("piapro_s", "")!!
            if (TextUtils.isEmpty(piapro_s_Cookie)) {
                throw LoginFailedException(LoginResult.UNKNOWN_ERR, "Cannot get the 'piapro_s' cookie from header.")
            }
            return piapro_s_Cookie
        } catch (ioe: IOException) {
            throw LoginFailedException(
                LoginResult.NETWORK_ERR,
                "Cannot get the 'piapro_s' cookie due to network error."
            )
        } catch (e: Exception) {
            throw LoginFailedException(
                LoginResult.NETWORK_ERR,
                "Cannot get the 'piapro_s' cookie due to unknown exception."
            )
        }
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