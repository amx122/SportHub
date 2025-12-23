package com.example.sporthub

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object FileUtils {
    fun uriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
            val bytes = outputStream.toByteArray()
            Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    fun fileToBase64(file: File): String? {
        return try {
            val bytes = file.readBytes()
            Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    fun base64ToFile(context: Context, base64: String): File? {
        return try {
            val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
            val tempFile = File.createTempFile("audio_play", ".mp3", context.cacheDir)
            val fos = FileOutputStream(tempFile)
            fos.write(decodedBytes)
            fos.close()
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}