package com.example.tictactoe.screens

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
        AvatarManager(profile).setAvatar(binding.ivHumanAvatar)

        binding.btSave.setOnClickListener { save() }
        binding.btCancel.setOnClickListener { navigator().goBack() }

        binding.vItemRandomMark.setOnClickListener { selectRandomMark() }
        binding.vItemTic.setOnClickListener { selectTic() }
        binding.vItemTac.setOnClickListener { selectTac() }

        binding.vItemRandomMove.setOnClickListener { selectRandomMove() }
        binding.vItemComputer.setOnClickListener { selectMoveComputer() }
        binding.vItemHuman.setOnClickListener { selectMoveHuman() }

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
        unSelectMarkViews()
        with(binding) {
            selectView(vItemTic, vTicBg, R.drawable.v_selected_tic, R.color.earthy_brown)
        }
    }

    private fun selectTac() {
        settings.humanTic = false
        unSelectMarkViews()
        with(binding) {
            selectView(vItemTac, vTacBg, R.drawable.v_selected_tac, R.color.deep_amethyst)
        }
    }

    private fun selectRandomMark() {
        settings.randomMark = true
        unSelectMarkViews()
        with(binding) {
            selectView(
                vItemRandomMark, vRandMarkBg, R.drawable.v_selected_random, R.color.midnight_blue
            )
        }
    }

    private fun selectRandomMove() {
        settings.randomMove = true
        unSelectMoveViews()
        with(binding) {
            selectView(
                vItemRandomMove,
                vRandMoveBg,
                R.drawable.v_selected_random,
                R.color.midnight_blue
            )
        }
    }

    private fun selectMoveComputer() {
        settings.humanMove = false
        unSelectMoveViews()
        with(binding) {
            selectView(
                vItemComputer,
                vComputerBg,
                R.drawable.v_selected_random,
                R.color.midnight_blue
            )
        }
    }

    private fun selectMoveHuman() {
        settings.humanMove = true
        unSelectMoveViews()
        with(binding) {
            selectView(
                vItemHuman,
                vHumanAvatarBg,
                R.drawable.v_selected_random,
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

    private fun unSelectMarkViews() {
        val markViews = arrayOf(binding.vItemRandomMark, binding.vItemTic, binding.vItemTac)
        for (markView in markViews) {
            restoreView(markView)
        }
        updateIconTint(binding.vRandMarkBg, R.color.deep_navy)
        updateIconTint(binding.vTicBg, R.color.smoky_brown)
        updateIconTint(binding.vTacBg, R.color.raspberry_purple)
    }

    private fun unSelectMoveViews() {
        val firstMoveViews =
            arrayOf(binding.vItemRandomMove, binding.vItemComputer, binding.vItemHuman)
        for (firstMoveView in firstMoveViews) {
            restoreView(firstMoveView)
        }
        updateIconTint(binding.vRandMoveBg, R.color.deep_navy)
        updateIconTint(binding.vComputerBg, R.color.deep_navy)
        updateIconTint(binding.vHumanAvatarBg, R.color.deep_navy)
    }

    private fun restoreView(
        view: View,
    ) {
        view.setBackgroundResource(R.drawable.v_board)
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