package com.example.tictactoe.fragments

import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.tictactoe.Profile
import com.example.tictactoe.contract.CustomAction
import com.example.tictactoe.contract.HasCustomAction
import com.example.tictactoe.contract.HasCustomTitle
import com.example.tictactoe.databinding.FragmentSettingsBinding
import com.example.tictactoe.R
import com.example.tictactoe.Settings
import com.example.tictactoe.contract.navigator
import com.example.tictactoe.extensions.getObject
import com.example.tictactoe.extensions.putObject
import com.example.tictactoe.utils.AvatarManager
import com.example.tictactoe.utils.SnackBarUtils

const val KEY_SETTINGS = "KEY_SETTINGS"

class SettingsFragment : Fragment(), HasCustomTitle, HasCustomAction {
    private lateinit var sharedPref: SharedPreferences
    private lateinit var profile: Profile
    private lateinit var settings: Settings
    private lateinit var binding: FragmentSettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = requireContext().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        profile = sharedPref.getObject(
            KEY_PROFILE, Profile.default(requireContext())
        )
        settings = sharedPref.getObject(KEY_SETTINGS, Settings())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        AvatarManager(profile).setAvatar(binding.avatar, binding.firstLetterTextView)

        binding.btnSave.setOnClickListener { save() }
        binding.btnCancel.setOnClickListener { navigator().goBack() }

        binding.randomMark.setOnClickListener { selectRandomMark() }
        binding.symbolTic.setOnClickListener { selectTic() }
        binding.symbolTac.setOnClickListener { selectTac() }

        binding.randomMove.setOnClickListener { selectRandomMove() }
        binding.computerMove.setOnClickListener { selectMoveComputer() }
        binding.humanMove.setOnClickListener { selectMoveHuman() }

        setupSettings()

        return binding.root
    }

    private fun updateView(
        view: View, @DrawableRes backgroundRes: Int
    ) {
        view.apply {
            backgroundTintList = null
            setBackgroundResource(backgroundRes)
        }
    }

    private fun selectTic() {
        settings.humanTic = true
        unSelectSymbolViews()
        with(binding) {
            selectView(symbolTic, icTic, R.drawable.selected_view_tic, R.color.earthy_brown)
        }
    }

    private fun selectTac() {
        settings.humanTic = false
        unSelectSymbolViews()
        with(binding) {
            selectView(symbolTac, icTac, R.drawable.selected_view_tac, R.color.deep_amethyst)
        }
    }

    private fun selectRandomMark() {
        settings.randomMark = true
        unSelectSymbolViews()
        with(binding) {
            selectView(
                randomMark, icRandMark, R.drawable.selected_view_random, R.color.midnight_blue
            )
        }
    }

    private fun selectRandomMove() {
        settings.randomMove = true
        unSelectMoveViews()
        with(binding) {
            selectView(
                randomMove,
                icRandMove,
                R.drawable.selected_view_random,
                R.color.midnight_blue
            )
        }
    }

    private fun selectMoveComputer() {
        settings.humanMove = false
        unSelectMoveViews()
        with(binding) {
            selectView(
                computerMove,
                icComputer,
                R.drawable.selected_view_random,
                R.color.midnight_blue
            )
        }
    }

    private fun selectMoveHuman() {
        settings.humanMove = true
        unSelectMoveViews()
        with(binding) {
            selectView(
                humanMove,
                icHuman,
                R.drawable.selected_view_random,
                R.color.midnight_blue
            )
        }
    }

    private fun selectView(
        view: View, iconView: View, @DrawableRes backgroundRes: Int, @ColorRes iconTintColorRes: Int
    ) {
        updateView(view, backgroundRes)
        updateIconTint(iconView, iconTintColorRes)
    }

    private fun unSelectSymbolViews() {
        val symbolViews = arrayOf(binding.randomMark, binding.symbolTic, binding.symbolTac)
        for (symbolView in symbolViews) {
            restoreView(symbolView)
        }
        updateIconTint(binding.icRandMark, R.color.deep_navy)
        updateIconTint(binding.icTic, R.color.smoky_brown)
        updateIconTint(binding.icTac, R.color.raspberry_purple)
    }

    private fun unSelectMoveViews() {
        val firstMoveViews =
            arrayOf(binding.humanMove, binding.computerMove, binding.randomMove)
        for (firstMoveView in firstMoveViews) {
            restoreView(firstMoveView)
        }
        updateIconTint(binding.icRandMove, R.color.deep_navy)
        updateIconTint(binding.icComputer, R.color.deep_navy)
        updateIconTint(binding.icHuman, R.color.deep_navy)
    }

    private fun restoreView(
        view: View,
    ) {
        view.setBackgroundResource(R.drawable.view_board_rounded)
        view.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(requireContext(), R.color.profile_background)
        )
    }

    private fun updateIconTint(view: View, @ColorRes colorRes: Int) {
        view.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(requireContext(), colorRes)
        )
    }


    private fun save() {
        with(sharedPref.edit()) {
            putObject(KEY_SETTINGS, settings)
            apply()
        }
        SnackBarUtils.showCustomSnackBar(
            binding.root,
            "Settings updated successfully!",
            "OK",
            ContextCompat.getColor(requireContext(), R.color.success_color)
        )
    }

    private fun setupSettings() {
        when {
            settings.randomMark -> selectRandomMark()
            settings.humanTic -> selectTic()
            else -> selectTac()
        }

        when {
            settings.randomMove -> selectRandomMove()
            settings.humanMove -> selectMoveHuman()
            else -> selectMoveComputer()
        }
    }

    override fun getTitleRes(): Int = R.string.toolbar_settings

    override fun getCustomAction(): CustomAction =
        CustomAction(R.drawable.ic_done, R.string.done) { save() }
}