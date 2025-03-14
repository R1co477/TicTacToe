package com.example.tictactoe.screens

import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.net.Uri
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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.tictactoe.Profile
import com.example.tictactoe.R
import com.example.tictactoe.contract.CustomAction
import com.example.tictactoe.contract.HasCustomAction
import com.example.tictactoe.contract.HasCustomTitle
import com.example.tictactoe.databinding.FragmentEditProfileBinding
import com.example.tictactoe.extensions.getObject
import com.example.tictactoe.extensions.putObject
import com.example.tictactoe.utils.SnackBarUtils


const val APP_PREFERENCES = "APP_PREFERENCES"
const val KEY_PROFILE = "KEY_PROFILE"


class EditProfileFragment : Fragment(), HasCustomTitle, HasCustomAction {
    private lateinit var sharedPref: SharedPreferences
    private lateinit var profile: Profile
    private lateinit var binding: FragmentEditProfileBinding

    private val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            setAvatar(uri)
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
        profile = sharedPref.getObject(
            KEY_PROFILE, Profile.default(requireContext())
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        binding.etNickname.addTextChangedListener(textWatcher)

        for (imageView in getImageViews()) {
            initializeImageView(imageView)
        }

        binding.btSelectPhoto.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }
        binding.btSaveChanges.setOnClickListener { saveChanges() }

        setupProfile(profile)
        updateUi()
        return binding.root
    }

    private fun initializeImageView(imageView: ImageView) {
        imageView.setOnClickListener {
            binding.tvAvatarLetter.visibility = View.VISIBLE
            unFocusAllViews()

            binding.ivHumanAvatar.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.profile_avatar)
            binding.ivHumanAvatar.backgroundTintList = imageView.backgroundTintList
            binding.ivHumanAvatar.backgroundTintMode = android.graphics.PorterDuff.Mode.SRC_IN

            imageView.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(), R.drawable.v_color_selected
                )
            )

            profile.selectedColor = imageView.backgroundTintList
            profile.avatarUri = null
        }
    }

    private fun setAvatar(uri: Uri) {
        unFocusAllViews()
        binding.tvAvatarLetter.visibility = View.INVISIBLE
        profile.avatarUri = uri
        binding.ivHumanAvatar.backgroundTintMode = null

        Glide.with(this).asDrawable().load(uri).circleCrop()
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable, transition: Transition<in Drawable>?
                ) {
                    binding.ivHumanAvatar.background = resource
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    private fun unFocusAllViews() {
        val iconDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.v_select_color)
        for (imageView in getImageViews()) {
            imageView.setImageDrawable(iconDrawable)
        }
    }

    private fun setupProfile(profile: Profile) {
        binding.etNickname.setText(profile.nickname)

        if (profile.avatarUri == null) {
            unFocusAllViews()
            setAvatarBackground(profile.selectedColor)
        } else {
            setAvatar(profile.avatarUri!!)
        }
    }

    private fun setAvatarBackground(tint: ColorStateList?) {
        for (imageView in getImageViews()) {
            if (imageView.backgroundTintList?.defaultColor == tint?.defaultColor) {
                imageView.callOnClick()
                break
            }
        }
    }

    private fun getImageViews(): List<ImageView> {
        return listOf(
            binding.ivRed,
            binding.ivGreen,
            binding.ivOrange,
            binding.ivPurple,
            binding.ivYellow,
            binding.ivBlue,
            binding.ivPink,
            binding.ivTeal
        )
    }

    private fun saveChanges() {
        if (binding.etNickname.error != null) {
            SnackBarUtils.showCustomSnackBar(
                binding.root,
                "The profile was not updated!",
                "OK",
                ContextCompat.getColor(requireContext(), R.color.error_color)
            )
            return
        }
        with(sharedPref.edit()) {
            putObject(KEY_PROFILE, profile)
            apply()
        }
        SnackBarUtils.showCustomSnackBar(
            binding.root,
            "Profile updated successfully!",
            "OK",
            ContextCompat.getColor(requireContext(), R.color.success_color)
        )


    }


    private fun updateUi() {
        val nickname = binding.etNickname.text.toString().trim()
        profile.nickname = nickname
        binding.tvAvatarLetter.text = if (nickname.isEmpty()) "" else nickname[0].toString()
        binding.etNickname.error = if (nickname.length > 15) "Nickname is too long" else null
        binding.tvNicknameSize.text = getString(R.string.char_сountTextView, nickname.length)
    }


    override fun getTitleRes(): Int = R.string.toolbar_edit_profile

    override fun getCustomAction(): CustomAction {
        return CustomAction(iconRes = R.drawable.ic_done,
            textRes = R.string.done,
            onCustomAction = { saveChanges() })
    }
}