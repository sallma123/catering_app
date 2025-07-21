package com.example.cateringapp.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object FileUtils {
    fun getFileFromUri(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)!!
        val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
        FileOutputStream(tempFile).use { output ->
            inputStream.copyTo(output)
        }
        return tempFile
    }
}
