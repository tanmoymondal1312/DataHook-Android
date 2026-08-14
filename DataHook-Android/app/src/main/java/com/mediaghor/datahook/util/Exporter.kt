package com.mediaghor.datahook.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import okhttp3.ResponseBody
import java.io.File

/** Writes an export payload to app cache and builds a share intent for it. */
object Exporter {

    fun writeToCache(context: Context, fileName: String, body: ResponseBody): File {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, fileName)
        body.byteStream().use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        return file
    }

    fun writeBytes(context: Context, fileName: String, bytes: ByteArray): File {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, fileName)
        file.writeBytes(bytes)
        return file
    }

    fun shareIntent(context: Context, file: File, mimeType: String): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
