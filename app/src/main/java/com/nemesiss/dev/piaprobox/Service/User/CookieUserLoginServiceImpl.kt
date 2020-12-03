package com.nemesiss.dev.piaprobox.Service.User

import android.content.Intent
import android.text.TextUtils
import com.nemesiss.dev.piaprobox.Activity.Common.LoginActivity
import com.nemesiss.dev.piaprobox.Activity.Common.LoginCallbackActivity
import com.nemesiss.dev.piaprobox.Model.Resources.Constants
import com.nemesiss.dev.piaprobox.Model.User.*
import com.nemesiss.dev.piaprobox.Service.HTMLParser
import com.nemesiss.dev.piaprobox.Service.Persistence
import com.nemesiss.dev.piaprobox.Service.SimpleHTTP.DaggerFetchFactory
import com.nemesiss.dev.piaprobox.Util.serverSetCookies
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*
import javax.inject.Inject


/**
 * Maintain Piapro's cookie for a login status.
 * This is a simple fashion also being used for Piapro's official site.
 */
class CookieUserLoginServiceImpl @Inject constructor(val httpClient: OkHttpClient, val htmlParser: HTMLParser) :
    UserLoginService {

    companion object {
        private val log = LoggerFactory.getLogger(CookieUserLoginServiceImpl::class.java)
    }

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
            log.info("Finish getting piapro_s: {}", piapro_s)
            // 2
            val loginCookie = getUserLoginCookie(piapro_s, loginCredentials)
            log.info("Finish getting login cookie: {}", loginCookie)
            // 登陆成功，保存登录态
            Persistence.SaveLoginCookie(loginCookie)
            Persistence.SaveLoginTimeStamp()
            Persistence.SaveLoginStatus(LoginStatus.LOGIN)
            log.info("try getting user info from piapro")
            val userInfo = getUserInfoFromPiapro()
            log.info("Finish getting user info from piapro: {}", userInfo)
            saveUserInfo(userInfo)
            return userInfo
        } catch (userInfoEx: GetUserInfoFailedException) {
            log.error("cannot get userinfo but we actually log in.", userInfoEx)
            throw userInfoEx
        } catch (e: Exception) {
            log.error("exception occurred while log in.", e)
            Persistence.RemoveLoginInfo()
            Persistence.SaveLoginStatus(LoginStatus.NOT_LOGIN)
            throw e
        }
    }

    override fun login(credentials: LoginCredentials): UserInfo {
        Persistence.SaveLoginCredentials(credentials)
        // Now we can call login method without parameters.
        return login()
    }

    override fun logout() {
        if (checkCachedLoginStatusValid()) {
            // cookie exists, need to do actual logout operations.
            val logoutRequest = buildLogoutRequest()
            try {
                val response = httpClient.newCall(logoutRequest).execute()
                if (response.code == 302) {
                    val serverCookies = response.serverSetCookies().associateBy { cookie -> cookie.name }
                    if (serverCookies.containsKey("piapro_r") && serverCookies.getValue("piapro_r").value == "deleted") {
                        log.info("Logout successful!")
                    } else {
                        log.warn("Logout server not response delete cookie directives!")
                    }
                } else {
                    log.error("Logout failed! Response is not successful! Response: {}", response)
                }
            } catch (ioe: IOException) {
                log.error("Log out failed! IOE: ", ioe)
            } catch (e: java.lang.Exception) {
                log.error("Log out failed! Unknown exception: ", e)
            }
        }
        else {
            log.warn("Log info is already expired. Just clean login info is enough.")
        }
        // anyway we clean our cache.
        Persistence.RemoveLoginInfo()
    }

    private fun getUserInfoFromPiapro(): UserInfo {
        // When we reach here we must have a correct login credentials so we can feel free to unwrap nullable ty
        val loginCredentials = Persistence.GetLoginCredentials()!!
        val userProfileUrl = Constants.Url.getUserProfileUrl(loginCredentials.userName)
        val response = DaggerFetchFactory.create().fetcher().withLoginCookie().visit(userProfileUrl).go()
        if (response.isSuccessful) {
            val html = Jsoup.parse(response.body?.string())
            val parseUserInfoSteps = htmlParser.Rules.getJSONObject("UserProfile").getJSONArray("Steps")
            val userInfo = htmlParser.Parser.GoSteps(html, parseUserInfoSteps) as UserInfo
            // Remove postfix 'さん' as we actually don't need it.
            userInfo.apply {
                nickName = nickName.replace("さん", "")
                avatarImage = HTMLParser.GetAlbumThumb(avatarImage)
            }
            return userInfo
        }
        log.error("get userinfo from piapro not successful, code: {}, response:{}", response.code, response)
        throw GetUserInfoFailedException(LoginResult.NETWORK_ERR, "Cannot get user info from Piapro.")
    }

    private fun buildLoginRequest(piapro_s_Cookie: String, loginCredentials: LoginCredentials): Request {
        val mediaType = "application/x-www-form-urlencoded".toMediaType()
        val body: RequestBody = loginCredentials.queryString.toRequestBody(mediaType)
        return Request.Builder()
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
    }

    private fun buildLogoutRequest(): Request {
        val cookie = Persistence.GetLoginCookie()!!
        return Request.Builder()
            .url("https://piapro.jp/logout/")
            .method("GET", null)
            .addHeader("Connection", "keep-alive")
            .addHeader(
                "sec-ch-ua",
                "\"\\Not;A\"Brand\";v=\"99\", \"Google Chrome\";v=\"85\", \"Chromium\";v=\"85\""
            )
            .addHeader("sec-ch-ua-mobile", "?0")
            .addHeader("Upgrade-Insecure-Requests", "1")
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
            .addHeader("Referer", "https://piapro.jp/nemesisslin")
            .addHeader("Accept-Language", "en-US,en;q=0.9")
            .addHeader(
                "Cookie",
                "piapro_s=${cookie.piapro_s}; piapro_r=${cookie.piapro_r}"
            )
            .build()
    }

    private fun getUserLoginCookie(piapro_s_Cookie: String, loginCredentials: LoginCredentials): LoginCookie {

        val request = buildLoginRequest(piapro_s_Cookie, loginCredentials)
        try {
            val response = httpClient.newCall(request).execute()
            val serverSetCookies = response.serverSetCookies().associateBy { cookie -> cookie.name }

            // must ensure server returns 'piapro_r' and the cookie value is not empty, or resulting an error.
            val haveValidUserCookie =
                serverSetCookies.containsKey("piapro_r") and !TextUtils.isEmpty(serverSetCookies["piapro_r"]?.value)

            if (!haveValidUserCookie) {
                throw LoginFailedException(
                    LoginResult.ACCOUNT_OR_PASSWORD_WRONG,
                    "Account or password error, because we cannot get 'piapro_r' from login response."
                )
            }
            // here we can unwrap the value of 'piapro_r' safely.
            val piapro_r = serverSetCookies.getValue("piapro_r")
            // here we are trying to update 'piapro_s' to a new value if possible.
            val piapro_s = serverSetCookies["piapro_s"]?.value ?: piapro_s_Cookie

            // if 'piapro_r' contains an expired time, we use it as cookie expired time, or we use a constant value
            // to avoid re-authentication.
            val expires =
                piapro_r.expires?.time ?: Date().time + Constants.Login.LOGIN_CACHE_VALID_TIME_INTERVAL_SEC * 1000L

            return LoginCookie(piapro_s, piapro_r.value!!, expires)
        } catch (ioe: IOException) {
            log.error("Do login execution IOE error.", ioe)
            throw LoginFailedException(
                LoginResult.NETWORK_ERR,
                "Cannot get the 'piapro_r' cookie due to network error."
            )
        } catch (e: Exception) {
            log.error("Do login execution error.", e)
            throw LoginFailedException(
                LoginResult.NETWORK_ERR,
                "Cannot get the 'piapro_r' cookie due to unknown exception."
            )
        }
    }

    private fun getPiapro_s(): String {
        // here we use the login page of Piapro instead of home page
        // because this page body is much smaller than home page (only 1/4 of home page),
        // and it can still provide a valid 'piapro_s' cookie.
        val request = Request.Builder().url(Constants.Url.LOGIN_PAGE).get().build()
        try {
            val response = httpClient.newCall(request).execute()
            val serverSetCookies = response.serverSetCookies().associateBy { cookie -> cookie.name }
            if (!serverSetCookies.containsKey("piapro_s") || TextUtils.isEmpty(serverSetCookies["piapro_s"]?.value)) {
                throw LoginFailedException(LoginResult.UNKNOWN_ERR, "Cannot get the 'piapro_s' cookie from header.")
            }
            return serverSetCookies["piapro_s"]?.value!!
        } catch (ioe: IOException) {
            log.error("get piapro_s IOE.", ioe)
            throw LoginFailedException(
                LoginResult.NETWORK_ERR,
                "Cannot get the 'piapro_s' cookie due to network error."
            )
        } catch (e: Exception) {
            log.error("get piapro_s error.", e)
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
        val loginCookies = Persistence.GetLoginCookie() ?: return LoginStatus.NOT_LOGIN
        if (!loginCookies.isValid) {
            return LoginStatus.NOT_LOGIN
        }
        /**
         * 1. 带着Cookie请求首页
         * 2. 看首页banner位置能否提取出用户信息
         */
        try {
            val response = DaggerFetchFactory.create().fetcher().visit(Constants.Url.MAIN_DOMAIN).withLoginCookie().go()
            if (response.isSuccessful) {
                val html = Jsoup.parse(response.body?.string())
                val userMenu = html.getElementsByClass("user_menu_mini")
                return if (userMenu.size > 0) LoginStatus.LOGIN else LoginStatus.LOGIN_EXPIRED
            }
            return LoginStatus.NOT_LOGIN
        } catch (e: java.lang.Exception) {
            // Log exception
            log.error("Check login status error.", e)
            return LoginStatus.NOT_LOGIN
        }
    }
}