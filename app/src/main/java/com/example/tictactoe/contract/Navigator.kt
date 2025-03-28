package com.example.tictactoe.contract

import androidx.fragment.app.Fragment


fun Fragment.navigator(): Navigator {
    return requireActivity() as Navigator
}

interface Navigator {

    fun showSingleGameScreen(level: Int)

    fun showDifficultyScreen()

    fun showEditProfileScreen()

    fun showSettingsMenuScreen()

    fun goBack()

    fun goToMenu()

    fun createCustomToolbarAction(action: CustomAction)
}