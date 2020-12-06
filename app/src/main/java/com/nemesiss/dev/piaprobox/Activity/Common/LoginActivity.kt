package com.nemesiss.dev.piaprobox.Activity.Common

import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.DaggerFactory.DaggerUserLoginServiceFactory
import com.nemesiss.dev.piaprobox.Service.DaggerModules.CookieLoginService
import com.nemesiss.dev.piaprobox.Service.DaggerModules.HtmlParserModules
import com.nemesiss.dev.piaprobox.Service.User.UserLoginService
import com.nemesiss.dev.piaprobox.Util.enableAutoHideSoftKeyboard
import com.nemesiss.dev.piaprobox.Util.getRootLayout
import com.nemesiss.dev.piaprobox.Util.getSystemService
import kotlinx.android.synthetic.main.activity_login.*
import javax.inject.Inject

class LoginActivity : PiaproboxBaseActivity() {


    @Inject
    @CookieLoginService
    lateinit var userLoginService: UserLoginService

    lateinit var imm: InputMethodManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        imm = getSystemService()

        getRootLayout<LinearLayout>().enableAutoHideSoftKeyboard()
        login_password_textinputfield.enableAutoHideSoftKeyboard(imm)
        login_username_textinputfield.enableAutoHideSoftKeyboard(imm)
        DaggerUserLoginServiceFactory.builder().htmlParserModules(HtmlParserModules(this)).build().inject(this)

        enter_login_button
            .setOnClickListener {
                testLoading()
                it.postDelayed({ testPending() }, 1000L)
            }
    }

    private fun testPending() {
        enter_login_button
            .apply {
                pending()
                enable()
            }
    }

    private fun testLoading() {
        enter_login_button
            .apply {
                loading()
                disable()
            }
    }

//         TODO Finish setting results like below:
//        val result = Intent().apply {
//            putExtra(Constants.Login.LOGIN_RESULT_KEY, LoginResult.SUCCESS.name)
//        }
//        setResult(Constants.Login.RESULT_CODE, result)
}
