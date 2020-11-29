package com.nemesiss.dev.piaprobox.Service

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.alibaba.fastjson.JSON
import com.nemesiss.dev.piaprobox.Model.User.LoginCookie
import com.nemesiss.dev.piaprobox.Model.User.LoginCredentials
import com.nemesiss.dev.piaprobox.Model.User.LoginStatus
import com.nemesiss.dev.piaprobox.Model.User.UserInfo
import java.sql.SQLPermission
import java.util.*

class Persistence {

    companion object {
        @JvmStatic
        val MUSIC_PLAYER_LOOP_STATUS = "MUSIC_PLAYER_LOOP_STATUS"

        @JvmStatic
        lateinit var SharedPref: SharedPreferences

        @JvmStatic
        lateinit var SharedPrefEditor: SharedPreferences.Editor

        internal object UserLoginServiceKey {
            const val LOGIN_CREDENTIALS = "LOGIN_CREDENTIALS"
            const val LOGIN_COOKIES = "LOGIN_COOKIES"
            const val LOGIN_STATUS = "LOGIN_STATUS"
            const val USER_INFO = "USER_INFO"
            const val LOGIN_TIMESTAMP = "LOGIN_TIMESTAMP"
        }

        @JvmStatic
        fun Init(context: Context) {
            SharedPref = android.preference.PreferenceManager.getDefaultSharedPreferences(context)
        }

        @JvmStatic
        fun SetMusicPlayerLoopStatus(Loop: Boolean) {
            SharedPrefEditor = SharedPref.edit()
            SharedPrefEditor.putBoolean(MUSIC_PLAYER_LOOP_STATUS, Loop)
            SharedPrefEditor.apply()
        }

        @JvmStatic
        fun GetMusicPlayerLoopStatus(): Boolean {
            return SharedPref.getBoolean(MUSIC_PLAYER_LOOP_STATUS, false)
        }

        fun GetLoginCookie(): LoginCookie? {
            return SharedPref.getString(UserLoginServiceKey.LOGIN_COOKIES, "")
                .let { credentials ->
                    if (TextUtils.isEmpty(credentials)) null
                    else JSON.parseObject(credentials, LoginCookie::class.java)
                }
        }

        fun SaveLoginCookie(cookie: LoginCookie): Boolean {
            SharedPrefEditor = SharedPref.edit()
            SharedPrefEditor.putString(UserLoginServiceKey.LOGIN_COOKIES, JSON.toJSONString(cookie))
            return SharedPrefEditor.commit()
        }

        fun SaveLoginCredentials(loginCredentials: LoginCredentials): Boolean {
            SharedPrefEditor = SharedPref.edit()
            SharedPrefEditor.putString(UserLoginServiceKey.LOGIN_CREDENTIALS, JSON.toJSONString(loginCredentials))
            return SharedPrefEditor.commit()
        }

        fun GetLoginCredentials(): LoginCredentials? {
            return SharedPref.getString(UserLoginServiceKey.LOGIN_CREDENTIALS, "")
                .let { credentials ->
                    if (TextUtils.isEmpty(credentials)) null
                    else JSON.parseObject(credentials, LoginCredentials::class.java)
                }
        }

        fun SaveLoginStatus(loginStatus: LoginStatus): Boolean {
            SharedPrefEditor = SharedPref.edit()
            SharedPrefEditor.putString(UserLoginServiceKey.LOGIN_STATUS, loginStatus.name)
            return SharedPrefEditor.commit()
        }

        fun GetLoginStatus(): LoginStatus? {
            return SharedPref.getString(UserLoginServiceKey.LOGIN_STATUS, "")
                .let { status -> if (TextUtils.isEmpty(status)) null else LoginStatus.valueOf(status) }
        }

        fun SaveUserInfo(userInfo: UserInfo): Boolean {
            SharedPrefEditor = SharedPref.edit()
            SharedPrefEditor.putString(UserLoginServiceKey.USER_INFO, JSON.toJSONString(userInfo))
            return SharedPrefEditor.commit()
        }

        fun GetUserInfo(): UserInfo? {
            return SharedPref.getString(UserLoginServiceKey.USER_INFO, "")
                .let { userInfo ->
                    if (TextUtils.isEmpty(userInfo)) null else JSON.parseObject(
                        userInfo,
                        UserInfo::class.java
                    )
                }
        }

        fun SaveLoginTimeStamp(): Boolean {
            SharedPrefEditor = SharedPref.edit()
            SharedPrefEditor.putLong(UserLoginServiceKey.LOGIN_TIMESTAMP, Date().time)
            return SharedPrefEditor.commit()
        }

        fun GetLoginTimeStamp(): Long {
            return SharedPref.getLong(UserLoginServiceKey.LOGIN_TIMESTAMP, 0)
        }
    }
}