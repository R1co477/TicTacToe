package com.example.tictactoe.contract

import androidx.annotation.StringRes
import com.example.tictactoe.GameState


interface HasCustomTitle {
    @StringRes
    fun getTitleRes(): Int
}