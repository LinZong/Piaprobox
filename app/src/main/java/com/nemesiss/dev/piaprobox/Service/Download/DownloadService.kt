package com.nemesiss.dev.piaprobox.Service.Download

import android.content.Context
import android.os.Environment
import android.util.Log
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.cause.ResumeFailedCause
import com.liulishuo.okdownload.core.listener.DownloadListener1
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist
import com.nemesiss.dev.piaprobox.Model.CheckPermissionModel
import com.nemesiss.dev.piaprobox.R
import com.nemesiss.dev.piaprobox.Service.AsyncExecutor
import com.nemesiss.dev.piaprobox.Service.DaggerFactory.DaggerDownloadServiceFactory
import com.nemesiss.dev.piaprobox.Util.PermissionUtil
import java.io.File
import java.lang.Exception
import javax.inject.Inject

class DownloadService @Inject constructor(val context: Context, val asyncExecutor: AsyncExecutor) {

    private val downloadNotificationManager = DownloadNotificationManager(context)

    companion object {
        @JvmStatic
        val MUSIC_DOWNLOAD_PATH = Environment.getExternalStorageDirectory().resolve("PiaproboxDownload")
    }

    fun DownloadMusic(fileName: String, URL: String, checker: CheckPermissionModel) {
        PermissionUtil.CheckStoragePermission(checker.fromActivity, object : BaseMultiplePermissionsListener() {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                asyncExecutor.SendTask {
                    _ActualDownloadMusic(fileName, URL)
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                PermissionUtil.ShowExplainDialog(
                    checker.fromActivity,
                    checker.fromActivity.resources.getString(R.string.NeedStroageAccessPermission),
                    checker.fromActivity.resources.getString(R.string.WhyNeedStroageAccessPermission),
                    { token?.continuePermissionRequest() },
                    { token?.cancelPermissionRequest() }
                )
            }
        })
    }

    private fun _ActualDownloadMusic(fileName: String, URL: String) {

//        DaggerDownloadServiceFactory.create()
        val FullPath = MUSIC_DOWNLOAD_PATH.resolve(fileName)
        _ActualDownloadFile(FullPath, URL)
    }

    private fun _ActualDownloadFile(filePath: File, URL: String) {

        val task = DownloadTask
            .Builder(URL, filePath)
            .setAutoCallbackToUIThread(true)
            .setPassIfAlreadyCompleted(true)
            .build()

        task.execute(object : DownloadListener1() {
            override fun taskStart(task: DownloadTask, model: Listener1Assist.Listener1Model) {
                downloadNotificationManager.SendDownloadNotification(filePath.name,0)
            }

            override fun taskEnd(
                task: DownloadTask,
                cause: EndCause,
                realCause: Exception?,
                model: Listener1Assist.Listener1Model
            ) {
                if(cause == EndCause.COMPLETED) {
                    downloadNotificationManager.SendDownloadFinishNotification(filePath.name)
                }
                else {
                    downloadNotificationManager.SendDownloadFailedNotification(filePath.name)
                }
            }

            override fun progress(task: DownloadTask, currentOffset: Long, totalLength: Long) {
                downloadNotificationManager.SendDownloadNotification(filePath.name, (currentOffset * 100 / totalLength).toInt())
            }

            override fun connected(task: DownloadTask, blockCount: Int, currentOffset: Long, totalLength: Long) {
            }

            override fun retry(task: DownloadTask, cause: ResumeFailedCause) {

            }
        })

    }

}

