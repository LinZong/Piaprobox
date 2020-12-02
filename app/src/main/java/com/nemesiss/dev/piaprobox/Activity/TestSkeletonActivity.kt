package com.nemesiss.dev.piaprobox.Activity

import android.os.Bundle
import com.nemesiss.dev.piaprobox.Activity.Common.PiaproboxBaseActivity
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.DaggerFactory.DaggerUserLoginServiceFactory
import com.nemesiss.dev.piaprobox.Service.DaggerModules.CookieLoginService
import com.nemesiss.dev.piaprobox.Service.DaggerModules.HtmlParserModules
import com.nemesiss.dev.piaprobox.Service.User.UserLoginService
import org.slf4j.LoggerFactory
import javax.inject.Inject


class TestSkeletonActivity : PiaproboxBaseActivity() {

    @Inject
    @CookieLoginService
    lateinit var userLoginService: UserLoginService

    private val log = LoggerFactory.getLogger(TestSkeletonActivity::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_skeleton)
        DaggerUserLoginServiceFactory.builder().htmlParserModules(HtmlParserModules(this)).build().inject(this)
        log.warn("Hello, world!")
        log.warn("Injected: {}", userLoginService)

//
//        val strWithUrl =
//            "ポッピンキャンディ☆フィーバー！(<a href=\"/jump/?url=https%3A%2F%2Fnico.ms%2Fsm35880454\" target=\"_blank\">https://nico.ms/sm35880454</a>)の歌詞になります。"
//
//        TextViewUtils.SetTextWithClickableUrl(Test_Parse_A_Tag, strWithUrl)
    }
}