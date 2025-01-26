package com.example.tictactoe.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.tictactoe.Profile
import com.example.tictactoe.R
import com.example.tictactoe.contract.CustomAction
import com.example.tictactoe.contract.HasCustomAction
import com.example.tictactoe.contract.HasCustomTitle
import com.example.tictactoe.databinding.FragmentEditProfileBinding

class EditProfileFragment : Fragment(), HasCustomTitle, HasCustomAction {
    private lateinit var profile: Profile
    private lateinit var binding: FragmentEditProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val tint = createTintList(
                ContextCompat.getColor(requireContext(), R.color.background_red),
                ContextCompat.getColor(requireContext(), R.color.background_red)
            )
            profile = Profile("Player", null, tint)
        } else {
            // release get profile with bundle
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        binding.edtNickname.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateUi()
            }

            override fun afterTextChanged(p0: Editable?) {  }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {  }
        })

        initializeImageView(binding.viewRed)
        initializeImageView(binding.viewTeal)
        initializeImageView(binding.viewGreen)
        initializeImageView(binding.viewBlue)
        initializeImageView(binding.viewPink)
        initializeImageView(binding.viewOrange)
        initializeImageView(binding.viewPurple)
        initializeImageView(binding.viewYellow)
        setupProfile(profile)
        updateUi()
        return binding.root
    }

    private fun initializeImageView(imageView: ImageView) {
        val iconDrawable =
            ContextCompat.getDrawable(requireContext(), R.drawable.view_color_selected)
        imageView.setOnClickListener {
            unfocusAllViews()
            imageView.setImageDrawable(iconDrawable)
            val tint = imageView.backgroundTintList
            binding.avatar.backgroundTintList = tint
        }
    }

    private fun unfocusAllViews() {
        val iconDrawable =
            ContextCompat.getDrawable(requireContext(), R.drawable.view_select_color)
        for (imageView in getImageViews()) {
            imageView.setImageDrawable(iconDrawable)
        }
    }

    private fun setupProfile(profile: Profile) {
        binding.edtNickname.setText(profile.nickname)

        if (profile.avatarUri != null) {
            // release setAvatar from location
        } else {
            unfocusAllViews()
            setAvatarBackground(profile.selectedColor)
        }

    }

    private fun setAvatarBackground(tint: ColorStateList?) {
        for (imageView in getImageViews()) {
            if (imageView.backgroundTintList!!.defaultColor == tint?.defaultColor) {
                imageView.callOnClick()
                break
            }
        }
    }

    private fun getImageViews(): List<ImageView> {
        return listOf(
            binding.viewRed,
            binding.viewGreen,
            binding.viewOrange,
            binding.viewPurple,
            binding.viewYellow,
            binding.viewBlue,
            binding.viewPink,
            binding.viewTeal
        )
    }

    private fun saveChanges() {

    }

    fun updateUi() {
        val nickname = binding.edtNickname.text.toString().trim()
        val nicknameLength = nickname.length
        binding.firstLetterTextView.text = if (nickname.isEmpty()) {
            ""
        } else {
            nickname[0].toString()
        }
        if (nicknameLength > 15) {
            binding.edtNickname.error = "Nickname is too long"
        } else {
            binding.edtNickname.error = null
        }
        binding.charCountTextView.text = getString(R.string.char_сountTextView, nickname.length)
    }

    private fun createTintList(enabledColor: Int, disabledColor: Int): ColorStateList {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_enabled),
            intArrayOf(-android.R.attr.state_enabled)
        )

        val colors = intArrayOf(
            enabledColor,
            disabledColor
        )

        return ColorStateList(states, colors)
    }


    override fun getTitleRes(): Int = R.string.edit_profile

    override fun getCustomAction(): CustomAction {
        return CustomAction(
            iconRes = R.drawable.ic_done,
            textRes = R.string.done,
            onCustomAction = {
                saveChanges()
            }
        )
    }
}