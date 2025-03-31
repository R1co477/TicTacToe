package com.example.tictactoe.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tictactoe.R
import com.example.tictactoe.contract.CustomAction
import com.example.tictactoe.contract.HasCustomAction
import com.example.tictactoe.contract.HasCustomTitle
import com.example.tictactoe.contract.navigator
import com.example.tictactoe.databinding.FragmentDifficultyBinding


class DifficultyFragment : Fragment(), HasCustomTitle, HasCustomAction {
    private lateinit var binding: FragmentDifficultyBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDifficultyBinding.inflate(inflater, container, false)
        binding.btEasy.setOnClickListener {
            navigator().showBotGameScreen(1)
        }
        binding.btMedium.setOnClickListener {
            navigator().showBotGameScreen(2)
        }
        binding.btDifficult.setOnClickListener {
            navigator().showBotGameScreen(3)
        }
        return binding.root
    }

    override fun getTitleRes(): Int = R.string.toolbar_difficulty
    override fun getCustomAction(): CustomAction = CustomAction(
        R.drawable.ic_settings,
        R.string.settings
    ) { navigator().showSettingsMenuScreen() }
}