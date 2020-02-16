package com.nemesiss.dev.piaprobox.Activity

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.RemoteViews
import com.nemesiss.dev.piaprobox.Activity.Common.PiaproboxBaseActivity
import com.nemesiss.dev.piaprobox.Model.CheckPermissionModel
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.DaggerFactory.DaggerDownloadServiceFactory
import com.nemesiss.dev.piaprobox.Service.DaggerModules.DownloadServiceModules
import com.nemesiss.dev.piaprobox.Service.Download.DownloadService
import com.nemesiss.dev.piaprobox.Service.HTMLParser
import com.nemesiss.dev.piaprobox.Service.MusicPlayer.MusicPlayerService
import com.nemesiss.dev.piaprobox.Util.AppUtil
import com.nemesiss.dev.piaprobox.View.Common.TextViewUtils
import com.nemesiss.dev.piaprobox.View.Spans.UrlClickableSpan
import kotlinx.android.synthetic.main.activity_test_skeleton.*
import javax.inject.Inject


class TestSkeletonActivity : PiaproboxBaseActivity() {

    @Inject
    lateinit var downloadService: DownloadService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_skeleton)

        val strWithUrl =
            "ポッピンキャンディ☆フィーバー！(<a href=\"/jump/?url=https%3A%2F%2Fnico.ms%2Fsm35880454\" target=\"_blank\">https://nico.ms/sm35880454</a>)の歌詞になります。"

        TextViewUtils.SetTextWithClickableUrl(Test_Parse_A_Tag, strWithUrl)
    }
}