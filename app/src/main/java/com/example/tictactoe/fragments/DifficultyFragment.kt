package com.example.tictactoe.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tictactoe.R
import com.example.tictactoe.contract.HasCustomTitle
import com.example.tictactoe.contract.navigator
import com.example.tictactoe.databinding.FragmentDifficultyBinding


class DifficultyFragment : Fragment(), HasCustomTitle {
    private lateinit var binding: FragmentDifficultyBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        binding = FragmentDifficultyBinding.inflate(inflater, container, false)
        binding.btnEasyy.setOnClickListener {
            navigator().showSingleGameScreen(1)
        }
        return binding.root
    }

    override fun getTitleRes(): Int = R.string.toolbar_difficulty

}