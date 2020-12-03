package com.nemesiss.dev.piaprobox.Activity

import android.os.Bundle
import android.view.View
import com.nemesiss.dev.piaprobox.Activity.Common.PiaproboxBaseActivity
import com.nemesiss.dev.piaprobox.Model.User.LoginCredentials
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.AsyncExecutor
import com.nemesiss.dev.piaprobox.Service.DaggerFactory.DaggerUserLoginServiceFactory
import com.nemesiss.dev.piaprobox.Service.DaggerModules.CookieLoginService
import com.nemesiss.dev.piaprobox.Service.DaggerModules.HtmlParserModules
import com.nemesiss.dev.piaprobox.Service.Persistence
import com.nemesiss.dev.piaprobox.Service.User.UserLoginService
import okhttp3.internal.wait
import org.slf4j.LoggerFactory
import java.lang.Exception
import javax.inject.Inject


class TestSkeletonActivity : PiaproboxBaseActivity() {

    @Inject
    @CookieLoginService
    lateinit var userLoginService: UserLoginService

    private val log = LoggerFactory.getLogger(TestSkeletonActivity::class.java)

    private val asyncExecutor = AsyncExecutor.INSTANCE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_skeleton)
        DaggerUserLoginServiceFactory.builder().htmlParserModules(HtmlParserModules(this)).build().inject(this)
        log.warn("Hello, world!")
        log.warn("Injected: {}", userLoginService)
    }

    fun login(view: View) {
        asyncExecutor.SendTask {
            try {
                val userInfo = userLoginService.login(LoginCredentials("nemesisslin", "lznb008828"))
                log.warn("Login finished. UserInfo: {}", userInfo)
            } catch (e: Exception) {
                log.error("Login failed!!", e)
            }
        }
    }

    fun readUserInfo(view: View) {
        log.warn("Userinfo: {}", Persistence.GetUserInfo())
        log.warn("Login status: {}", Persistence.GetLoginStatus())
        log.warn("LoginCookie: {}", Persistence.GetLoginCookie())
        log.warn("LoginCredentials: {}", Persistence.GetLoginCredentials())
    }

    fun logout(view: View) {
        asyncExecutor.SendTask {
            userLoginService.logout()
        }
    }
}