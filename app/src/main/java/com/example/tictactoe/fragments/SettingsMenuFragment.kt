package com.example.tictactoe.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tictactoe.Profile
import com.example.tictactoe.contract.CustomAction
import com.example.tictactoe.contract.HasCustomAction
import com.example.tictactoe.contract.HasCustomTitle
import com.example.tictactoe.databinding.FragmentSettingsMenuBinding
import com.example.tictactoe.R
import com.example.tictactoe.Settings
import com.example.tictactoe.contract.navigator
import com.example.tictactoe.extensions.getObject
import com.example.tictactoe.extensions.putObject
import com.example.tictactoe.utils.AvatarManager

const val KEY_SETTINGS = "KEY_SETTINGS"

class SettingsMenuFragment : Fragment(), HasCustomTitle, HasCustomAction {
    private lateinit var sharedPref: SharedPreferences
    private lateinit var profile: Profile
    private lateinit var settings: Settings
    private lateinit var binding: FragmentSettingsMenuBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = requireContext().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        profile = sharedPref.getObject(
            KEY_PROFILE, Profile.default(requireContext())
        )
        settings = sharedPref.getObject(KEY_SETTINGS, Settings())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsMenuBinding.inflate(inflater, container, false)
        AvatarManager(profile).setAvatar(binding.avatar, binding.firstLetterTextView)
        binding.btnSave.setOnClickListener { saveSettings() }
        binding.btnCancel.setOnClickListener{ navigator().goBack() }
        binding.randomSymbol.setOnClickListener { settings.isRandomMarkAssignment = true }
        binding.randomFirstMove.setOnClickListener { settings.isRandomFirstMove = true }

        setupSettings()

        return binding.root
    }

    private fun saveSettings() {
        with(sharedPref.edit()) {
            putObject(KEY_SETTINGS, settings)
            apply()
        }
    }

    private fun setupSettings() {

    }

    override fun getTitleRes(): Int = R.string.toolbar_settings

    override fun getCustomAction(): CustomAction =
        CustomAction(R.drawable.ic_done, R.string.done, { saveSettings() })
}