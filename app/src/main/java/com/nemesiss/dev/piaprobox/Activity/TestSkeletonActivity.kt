package com.nemesiss.dev.piaprobox.Activity

import android.os.Bundle
import com.nemesiss.dev.piaprobox.Activity.Common.PiaproboxBaseActivity
import com.nemesiss.dev.piaprobox.R


class TestSkeletonActivity : PiaproboxBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_skeleton)
//
//        val strWithUrl =
//            "ポッピンキャンディ☆フィーバー！(<a href=\"/jump/?url=https%3A%2F%2Fnico.ms%2Fsm35880454\" target=\"_blank\">https://nico.ms/sm35880454</a>)の歌詞になります。"
//
//        TextViewUtils.SetTextWithClickableUrl(Test_Parse_A_Tag, strWithUrl)
    }
}