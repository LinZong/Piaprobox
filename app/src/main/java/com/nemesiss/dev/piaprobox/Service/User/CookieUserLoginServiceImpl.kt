package com.nemesiss.dev.piaprobox.Service.User

import com.nemesiss.dev.piaprobox.Model.User.LoginCredentials
import com.nemesiss.dev.piaprobox.Model.User.LoginStatus
import com.nemesiss.dev.piaprobox.Model.User.UserInfo
import javax.inject.Inject

/**
 * Maintain Piapro's cookie for a login status.
 * This is a simple fashion also being used for Piapro's official site.
 */
class CookieUserLoginServiceImpl @Inject constructor() : UserLoginService {

    override fun getUserInfo(): UserInfo {
        TODO("Not yet implemented")
    }

    override fun getLoginCredentials(): LoginCredentials {
        TODO("Not yet implemented")
    }

    override fun saveLoginCredentials(credentials: LoginCredentials): Boolean {
        TODO("Not yet implemented")
    }

    override fun login(): UserInfo {
        TODO("Not yet implemented")
    }

    override fun login(credentials: LoginCredentials): UserInfo {
        TODO("Not yet implemented")
    }

    override fun startLoginActivity() {
        TODO("Not yet implemented")
    }

    override fun checkLogin(): LoginStatus {
        TODO("Not yet implemented")
    }
}