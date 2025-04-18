package com.example.tictactoe.data

import android.content.Context
import android.content.res.ColorStateList
import android.net.Uri
import com.example.tictactoe.R

class Profile(var nickname: String, var avatarUri: Uri?, var selectedColor: ColorStateList?) {
    companion object {
        fun default(context: Context): Profile = Profile(
            "Player", null, ColorStateList.valueOf(context.getColor(R.color.background_red))
        )
    }
}