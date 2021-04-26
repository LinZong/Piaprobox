package com.nemesiss.dev.piaprobox.Activity.Common

import android.content.Intent
import android.text.TextUtils
import com.nemesiss.dev.piaprobox.model.resources.Constants
import com.nemesiss.dev.piaprobox.model.user.LoginResult
import com.nemesiss.dev.piaprobox.model.user.UserInfo

abstract class LoginCallbackActivity : PiaproboxBaseActivity() {

    abstract fun handleLoginResult(loginResult: LoginResult, userInfo: UserInfo?)

    /**
     * dispatch login result to handler.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // match two code to identify the Activity result.
        if (requestCode == Constants.Login.REQUEST_CODE && resultCode == Constants.Login.RESULT_CODE) {
            val loginResultName = data?.getStringExtra(Constants.Login.LOGIN_RESULT_KEY)
            val loginUserInfo = data?.getParcelableExtra<UserInfo>(Constants.Login.LOGIN_RESULT_USERINFO_PAYLOAD_KEY)
            val loginResult =
                if (TextUtils.isEmpty(loginResultName)) LoginResult.UNKNOWN_ERR else LoginResult.valueOf(loginResultName!!)
            handleLoginResult(loginResult, loginUserInfo)
        }
    }
}