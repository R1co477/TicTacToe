package com.example.tictactoe
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Profile(var nickname: String, var avatarUri: Uri?, var selectedColor: ColorStateList?) : Parcelable