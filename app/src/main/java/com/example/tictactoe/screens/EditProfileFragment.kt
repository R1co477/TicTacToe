package com.example.tictactoe.screens

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.tictactoe.Profile
import com.example.tictactoe.R
import com.example.tictactoe.contract.CustomAction
import com.example.tictactoe.contract.HasCustomAction
import com.example.tictactoe.contract.HasCustomTitle
import com.example.tictactoe.databinding.FragmentEditProfileBinding
import com.example.tictactoe.extensions.getObject
import com.example.tictactoe.extensions.putObject
import com.example.tictactoe.utils.AvatarManager
import com.example.tictactoe.utils.SnackBarUtils

const val APP_PREFERENCES = "APP_PREFERENCES"
const val KEY_PROFILE = "KEY_PROFILE"

class EditProfileFragment : Fragment(), HasCustomTitle, HasCustomAction {
    private lateinit var sharedPref: SharedPreferences
    private lateinit var profile: Profile
    private lateinit var binding: FragmentEditProfileBinding

    private val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
        uri?.let {
            requireContext().contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            unFocusAllViews()
            profile.avatarUri = it
            profile.selectedColor = null
            setAvatar()
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            updateUi()
        }

        override fun afterTextChanged(p0: Editable?) {}
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = requireContext().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        profile = sharedPref.getObject(KEY_PROFILE, Profile.default(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        binding.etNickname.addTextChangedListener(textWatcher)

        getImageViews().forEach { setupColorSelection(it) }

        binding.btSelectPhoto.setOnClickListener {
            pickMedia.launch(
                PickVisualMediaRequest(
                    PickVisualMedia.ImageOnly
                )
            )
        }
        binding.btSaveChanges.setOnClickListener { saveChanges() }

        setupProfile()
        return binding.root
    }

    private fun setupColorSelection(imageView: ImageView) {
        imageView.setOnClickListener {
            unFocusAllViews()
            imageView.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.v_color_selected)
            )
            profile.selectedColor = imageView.backgroundTintList
            profile.avatarUri = null
            setAvatar()
        }
    }

    private fun setAvatar() {
        AvatarManager(profile).setAvatar(binding.ivHumanAvatar)
    }

    private fun unFocusAllViews() {
        val iconDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.v_color)
        getImageViews().forEach { it.setImageDrawable(iconDrawable) }
    }

    private fun setupProfile() {
        binding.etNickname.setText(profile.nickname)
        setAvatar()
        profile.selectedColor?.let { selectedColor ->
            getImageViews().forEach { imageView ->
                if (imageView.backgroundTintList?.defaultColor == selectedColor.defaultColor) {
                    imageView.performClick()
                    return
                }
            }
        }
    }

    private fun getImageViews(): List<ImageView> = listOf(
        binding.ivRed,
        binding.ivGreen,
        binding.ivOrange,
        binding.ivPurple,
        binding.ivYellow,
        binding.ivBlue,
        binding.ivPink,
        binding.ivTeal
    )

    private fun saveChanges() {
        if (binding.etNickname.error != null) {
            SnackBarUtils.showCustomSnackBar(
                binding.root, "The profile was not updated!", "OK",
                ContextCompat.getColor(requireContext(), R.color.error_color)
            )
            return
        }
        with(sharedPref.edit()) {
            putObject(KEY_PROFILE, profile)
            apply()
        }
        SnackBarUtils.showCustomSnackBar(
            binding.root, "Profile updated successfully!", "OK",
            ContextCompat.getColor(requireContext(), R.color.success_color)
        )
    }

    private fun updateUi() {
        val nickname = binding.etNickname.text.toString().trim()
        profile.nickname = nickname
        binding.etNickname.error = if (nickname.length > 15) "Nickname is too long" else null
        binding.tvNicknameSize.text = getString(R.string.char_count_tv, nickname.length)
        setAvatar()
    }

    override fun getTitleRes(): Int = R.string.toolbar_edit_profile

    override fun getCustomAction(): CustomAction {
        return CustomAction(R.drawable.ic_done, R.string.done) { saveChanges() }
    }
}
