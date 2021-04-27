package com.nemesiss.dev.piaprobox.Service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Environment
import java9.util.concurrent.CompletableFuture
import okhttp3.internal.closeQuietly
import org.slf4j.getLogger
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.Future
import javax.inject.Inject

class ImageCache @Inject constructor(private val context: Context) {

    private companion object {
        private val log = getLogger<ImageCache>()
    }

    private val cacheDir = File(context.filesDir, "ImageCache")
    private val async = AsyncExecutor.INSTANCE

    init {
        if (!cacheDir.exists()) cacheDir.mkdirs()
    }


    fun cache(drawable: Drawable, fileName: String): Future<Boolean> {
        if (drawable is BitmapDrawable) {
            val bitmap = drawable.bitmap
            return cache(bitmap, fileName)
        }
        val bounds = drawable.bounds
        val width = bounds.right - bounds.left
        val height = bounds.bottom - bounds.top

        if (width <= 0 || height <= 0) {
            return CompletableFuture<Boolean>().apply { complete(false) }
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.draw(canvas)
        return cache(bitmap, fileName)
    }

    fun cache(bitmap: Bitmap, fileName: String): Future<Boolean> {
        val format = detectCompressFormat(fileName)
        return async.SendTaskWithResult task@ {
            // duplicate bitmap for safe.
            val cloneBitmap = Bitmap.createBitmap(bitmap)
            val fos = FileOutputStream(File(cacheDir, fileName))
            try {
                cloneBitmap.compress(format, 100, fos)
                return@task true
            } catch (t: Throwable) {
                log.error("Cannot save $fileName", t)
                return@task false
            } finally {
                fos.closeQuietly()
                cloneBitmap.recycle()
            }
        }
    }

    fun cache(byteArray: ByteArray, fileName: String): Future<Boolean> {
        // no used, just ensure extension exists in file name.
        detectCompressFormat(fileName)
        return async.SendTaskWithResult task@ {
            val fos = FileOutputStream(File(cacheDir, fileName))
            try {
                fos.write(byteArray)
                return@task true
            } catch (t: Throwable) {
                log.error("Cannot save byte array for $fileName", t)
                return@task false
            } finally {
                fos.closeQuietly()
            }
        }
    }

    operator fun get(fileName: String): ByteArray? {
        val cacheFile = File(cacheDir, fileName)
        if (!cacheFile.exists()) return null
        val fis = FileInputStream(cacheFile)
        val result = fis.readBytes()
        fis.closeQuietly()
        return result
    }

    private fun detectCompressFormat(fileName: String): Bitmap.CompressFormat {
        val extDotIndex = fileName.lastIndexOf('.')
        if (extDotIndex == -1) {
            throw IllegalArgumentException("Filename $fileName must include format extension like png, jpg or webp.")
        }
        var extension = fileName.substring(extDotIndex + 1)
        extension = extension.trim().toLowerCase(Locale.CHINA)

        return when (extension) {
            "png" -> Bitmap.CompressFormat.PNG
            "jpg", "jpeg" -> Bitmap.CompressFormat.JPEG
            "webp" -> Bitmap.CompressFormat.WEBP
            else -> throw IllegalArgumentException("Filename $fileName must include format extension like png, jpg or webp.")
        }
    }
}