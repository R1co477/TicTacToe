package com.example.tictactoe.contract

import androidx.fragment.app.Fragment
import com.example.tictactoe.data.EntityCard
import com.example.tictactoe.data.ResultGame
import com.example.tictactoe.fragments.OnRefreshClick


fun Fragment.navigator(): Navigator {
    return requireActivity() as Navigator
}

interface Navigator {

    fun showBotGameScreen(level: Int)

    fun showDifficultyScreen()

    fun showEditProfileScreen()

    fun showSettingsMenuScreen()

    fun showLocalMultiplayerScreen()

    fun showGameOverScreen(
        humanEntityCard: EntityCard,
        opponentEntityCard: EntityCard,
        resultGame: ResultGame,
        onRefreshClick: OnRefreshClick
    )

    fun goBack()

    fun goToMenu()

    fun createCustomToolbarAction(action: CustomAction)
}