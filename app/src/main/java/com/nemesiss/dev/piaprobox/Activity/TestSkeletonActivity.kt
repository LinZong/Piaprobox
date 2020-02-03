package com.nemesiss.dev.piaprobox.Activity

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.view.View
import android.widget.RemoteViews
import com.nemesiss.dev.piaprobox.Activity.Common.PiaproboxBaseActivity
import com.nemesiss.dev.piaprobox.Model.CheckPermissionModel
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.DaggerFactory.DaggerDownloadServiceFactory
import com.nemesiss.dev.piaprobox.Service.DaggerModules.DownloadServiceModules
import com.nemesiss.dev.piaprobox.Service.Download.DownloadService
import com.nemesiss.dev.piaprobox.Service.MusicPlayer.MusicPlayerService
import javax.inject.Inject


class TestSkeletonActivity : PiaproboxBaseActivity() {

    @Inject
    lateinit var downloadService : DownloadService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_skeleton)

        DaggerDownloadServiceFactory.builder()
            .downloadServiceModules(DownloadServiceModules(this))
            .build()
            .inject(this)

    }

    fun SendNotification(view: View) {
        downloadService.DownloadMusic("Relax.pdf","http://192.168.1.3:5000/book.pdf", CheckPermissionModel(this))
    }
}