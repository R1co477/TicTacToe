package com.example.tictactoe

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import com.example.tictactoe.utils.bitmapToByteArray
import com.example.tictactoe.utils.byteArrayToBitmap

class EntityCard(val name: String, val description: String, val avatar: Bitmap?, val mark: Int) : Parcelable {
    private val avatarData: ByteArray = avatar?.let { bitmapToByteArray(it) } ?: ByteArray(0)

    constructor(parcel: Parcel) : this(
        name = parcel.readString() ?: "",
        description = parcel.readString() ?: "",
        avatar = parcel.createByteArray()?.let { if (it.isNotEmpty()) byteArrayToBitmap(it) else null },
        mark = parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeByteArray(avatarData)
        parcel.writeInt(mark)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<EntityCard> {
        override fun createFromParcel(parcel: Parcel): EntityCard = EntityCard(parcel)
        override fun newArray(size: Int): Array<EntityCard?> = arrayOfNulls(size)
    }
}