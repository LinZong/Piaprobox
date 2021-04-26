package com.nemesiss.dev.piaprobox.Activity.Common

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import com.nemesiss.dev.piaprobox.model.resources.Constants
import com.nemesiss.dev.piaprobox.model.user.LoginCredentials
import com.nemesiss.dev.piaprobox.model.user.LoginResult
import com.nemesiss.dev.piaprobox.model.user.UserInfo
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.AsyncExecutor
import com.nemesiss.dev.piaprobox.Service.DaggerFactory.DaggerUserLoginServiceFactory
import com.nemesiss.dev.piaprobox.Service.DaggerModules.CookieLoginService
import com.nemesiss.dev.piaprobox.Service.DaggerModules.HtmlParserModules
import com.nemesiss.dev.piaprobox.Service.User.GetUserInfoFailedException
import com.nemesiss.dev.piaprobox.Service.User.LoginFailedException
import com.nemesiss.dev.piaprobox.Service.User.UserLoginService
import com.nemesiss.dev.piaprobox.util.enableAutoHideSoftKeyboard
import com.nemesiss.dev.piaprobox.util.getRootLayout
import com.nemesiss.dev.piaprobox.util.getSystemService
import kotlinx.android.synthetic.main.activity_login.*
import org.slf4j.getLogger
import javax.inject.Inject

class LoginActivity : PiaproboxBaseActivity() {

    @Inject
    @CookieLoginService
    lateinit var userLoginService: UserLoginService

    lateinit var imm: InputMethodManager

    private val asyncExecutor = AsyncExecutor.INSTANCE

    private val log = getLogger<LoginActivity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initComponents()
        bindComponents()
    }

    private fun initComponents() {
        imm = getSystemService()
        getRootLayout<LinearLayout>().enableAutoHideSoftKeyboard()
        login_activity_login_password_textinputfield.enableAutoHideSoftKeyboard(imm)
        login_activity_login_username_textinputfield.enableAutoHideSoftKeyboard(imm)
        DaggerUserLoginServiceFactory.builder().htmlParserModules(HtmlParserModules(this)).build().inject(this)
    }

    private fun bindComponents() {
        login_activity_close.setOnClickListener { onBackPressed() }
        login_activity_start_login_button
            .setOnClickListener {
                startLoginRequest()
                arrayOf(
                    login_activity_login_password_textinputfield,
                    login_activity_login_username_textinputfield
                ).forEach { et -> et.clearFocus() }
            }
    }

    private fun startLoginRequest() {
        val loginCredentials = collectUserLoginCredentials()
        if (loginCredentials.userName.isEmpty() || loginCredentials.password.isEmpty()) {
            showSnakeHint(getString(R.string.login_username_or_password_cannot_be_empty))
            return
        }
        disableLoginButton()
        asyncExecutor.SendTask {
            try {
                val userInfo = userLoginService.login(loginCredentials)
                setActivityCallbackLoginResult(LoginResult.SUCCESS, userInfo)
                finish()
            } catch (loginFailedEx: LoginFailedException) {
                log.error("Login with exception!", loginFailedEx)
                handleFailedLoginResultFromUserLoginService(loginFailedEx.loginResult)
            } catch (getUserInfoFailedEx: GetUserInfoFailedException) {
                log.error("Login with exception!", getUserInfoFailedEx)
                // 只是拿不到用户信息, 这里仍然可以认为成功返回，到了外面再拿一次
                setActivityCallbackLoginResult(LoginResult.SUCCESS)
                finish()
            } finally {
                enableLoginButton()
            }
        }
    }

    private fun disableLoginButton() {
        runOnUiThread {
            login_activity_start_login_button.apply {
                loading()
                disable()
            }
        }
    }

    private fun enableLoginButton() {
        runOnUiThread {
            login_activity_start_login_button.apply {
                pending()
                enable()
            }
        }
    }

    private fun handleFailedLoginResultFromUserLoginService(loginResult: LoginResult) {
        runOnUiThread {
            when (loginResult) {
                LoginResult.ACCOUNT_OR_PASSWORD_WRONG -> showSnakeHint(getString(R.string.login_failed_username_or_password_wrong))
                LoginResult.NETWORK_ERR -> showSnakeHint(getString(R.string.login_failed_network_error))
                LoginResult.UNKNOWN_ERR -> showSnakeHint(getString(R.string.login_failed_unknown_error))
            }
        }
    }

    private fun collectUserLoginCredentials(): LoginCredentials {
        val username = login_activity_login_username_textinputfield.text ?: ""
        val password = login_activity_login_password_textinputfield.text ?: ""
        return LoginCredentials(username.toString(), password.toString())
    }

    private fun setActivityCallbackLoginResult(loginResult: LoginResult, userInfo: UserInfo) {
        val result = Intent().apply {
            putExtra(Constants.Login.LOGIN_RESULT_KEY, loginResult.name)
            putExtra(Constants.Login.LOGIN_RESULT_USERINFO_PAYLOAD_KEY, userInfo)
        }
        setResult(Constants.Login.RESULT_CODE, result)
    }

    private fun setActivityCallbackLoginResult(loginResult: LoginResult) {
        val result = Intent().apply {
            putExtra(Constants.Login.LOGIN_RESULT_KEY, loginResult.name)
        }
        setResult(Constants.Login.RESULT_CODE, result)
    }

    private fun showSnakeHint(hintText: String) {
        Snackbar.make(findViewById(android.R.id.content), hintText, Snackbar.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        setActivityCallbackLoginResult(LoginResult.CANCELED_BY_USER)
        super.onBackPressed()
    }
}