package com.example.tictactoe
import android.content.res.ColorStateList
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Profile(val nickname: String, val avatarUri: String?, val selectedColor: ColorStateList?) : Parcelable