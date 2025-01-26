package com.example.tictactoe.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tictactoe.R
import com.example.tictactoe.contract.CustomAction
import com.example.tictactoe.contract.HasCustomTitle
import com.example.tictactoe.contract.HasCustomAction
import com.example.tictactoe.contract.navigator
import com.example.tictactoe.databinding.FragmentMenuBinding

class MenuFragment : Fragment(), HasCustomAction {
    private lateinit var binding: FragmentMenuBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        binding = FragmentMenuBinding.inflate(inflater, container, false)
        binding.btnSinglePlayer.setOnClickListener {
            navigator().showDifficultyScreen()
        }
        return binding.root
    }

    override fun getCustomAction(): CustomAction {
        return CustomAction(
            iconRes = R.drawable.profile_icon,
            textRes = R.string.player_avatar,
            onCustomAction = Runnable { navigator().showEditProfileScreen() }
        )
    }
}