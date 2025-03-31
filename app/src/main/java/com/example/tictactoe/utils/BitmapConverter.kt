package com.example.tictactoe.utils

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.drawable.VectorDrawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.graphics.createBitmap
import java.io.ByteArrayOutputStream
import java.io.IOException

internal fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}

internal fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
    return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
}


@Suppress("DEPRECATION")
private fun getBitmapLegacy(contentResolver: ContentResolver, fileUri: Uri): Bitmap? {
    var bitmap: Bitmap? = null
    try {
        bitmap = MediaStore.Images.Media.getBitmap(contentResolver, fileUri)
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return bitmap
}

@RequiresApi(Build.VERSION_CODES.P)
private fun getBitmapImageDecoder(contentResolver: ContentResolver, fileUri: Uri): Bitmap? {
    var bitmap: Bitmap? = null
    try {
        bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, fileUri))
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return bitmap
}

internal fun uriToBitmap(contentResolver: ContentResolver, fileUri: Uri?): Bitmap? {
    if (fileUri == null) {
        return null
    }

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        getBitmapImageDecoder(contentResolver, fileUri)
    } else {
        getBitmapLegacy(contentResolver, fileUri)
    }
}

fun vectorDrawableToBitmap(vectorDrawable: VectorDrawable): Bitmap {
    val bitmap = createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)

    val canvas = Canvas(bitmap)
    vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
    vectorDrawable.draw(canvas)

    return bitmap
}