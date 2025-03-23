package com.example.tictactoe

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class ResultGame(val bitmap: Bitmap?, val result: String) : Parcelable