package com.nemesiss.dev.piaprobox.Activity.Common

import android.content.Intent
import android.os.Bundle
import com.nemesiss.dev.piaprobox.Model.Resources.Constants
import com.nemesiss.dev.piaprobox.Model.User.LoginResult
import com.nemesiss.dev.piaprobox.R

class LoginActivity : PiaproboxBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // TODO Finish setting results like below:
        val result = Intent().apply {
            putExtra(Constants.Login.LOGIN_RESULT_KEY, LoginResult.SUCCESS.name)
        }
        setResult(Constants.Login.RESULT_CODE, result)
    }
}
