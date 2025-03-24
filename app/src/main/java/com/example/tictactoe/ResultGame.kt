package com.example.tictactoe

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import com.example.tictactoe.utils.bitmapToByteArray
import com.example.tictactoe.utils.byteArrayToBitmap

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
