package com.example.tictactoe

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Parcel
import android.os.Parcelable
import java.io.ByteArrayOutputStream

class ResultGame(val bitmap: Bitmap?, val result: String) : Parcelable {
    val bitmapData: ByteArray? = bitmap?.let { bitmapToByteArray(it) }

    constructor(parcel: Parcel) : this(
        parcel.createByteArray()?.let { byteArrayToBitmap(it) },
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByteArray(bitmapData)
        parcel.writeString(result)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ResultGame> {
        override fun createFromParcel(parcel: Parcel): ResultGame = ResultGame(parcel)
        override fun newArray(size: Int): Array<ResultGame?> = arrayOfNulls(size)
    }
}

private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}

private fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
    return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
}
