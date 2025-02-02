package com.example.tictactoe.fragments

import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.ColorSpace.Rgb
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
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
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import com.example.tictactoe.extensions.getObject
import com.example.tictactoe.extensions.putObject

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
            KEY_PROFILE, Profile(
                "Player", null, createTintList(
                    ContextCompat.getColor(requireContext(), R.color.background_red),
                    ContextCompat.getColor(requireContext(), R.color.background_red)
                )
            )
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        binding.edtNickname.addTextChangedListener(textWatcher)

        for (imageView in getImageViews()) {
            initializeImageView(imageView)
        }

        binding.imgSelectPhoto.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }
        binding.btnSaveChanges.setOnClickListener { saveChanges() }

        setupProfile(profile)
        updateUi()
        return binding.root
    }

    private fun initializeImageView(imageView: ImageView) {
        imageView.setOnClickListener {
            val iconDrawable =
                ContextCompat.getDrawable(requireContext(), R.drawable.view_color_selected)
            binding.firstLetterTextView.visibility = View.VISIBLE
            unFocusAllViews()
            binding.avatar.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.profile_avatar)
            binding.avatar.backgroundTintList = imageView.backgroundTintList
            binding.avatar.backgroundTintMode = android.graphics.PorterDuff.Mode.SRC_IN
            imageView.setImageDrawable(iconDrawable)
            profile.selectedColor = imageView.backgroundTintList
            profile.avatarUri = null
        }
    }

    private fun setAvatar(uri: Uri) {
        unFocusAllViews()
        binding.firstLetterTextView.visibility = View.INVISIBLE
        profile.avatarUri = uri
        binding.avatar.background = null
        binding.avatar.backgroundTintMode = null
        Glide.with(this).asDrawable().load(uri).circleCrop()
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable, transition: Transition<in Drawable>?
                ) {
                    binding.avatar.background = resource
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    private fun unFocusAllViews() {
        val iconDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.view_select_color)
        for (imageView in getImageViews()) {
            imageView.setImageDrawable(iconDrawable)
        }
    }

    private fun setupProfile(profile: Profile) {
        binding.edtNickname.setText(profile.nickname)

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
        with(sharedPref.edit()) {
            putObject(KEY_PROFILE, profile)
            apply()
        }
        showCustomToast()
    }
    private fun showCustomToast() {
        val inflater = LayoutInflater.from(requireContext())
        val layout = inflater.inflate(R.layout.custom_toast, requireActivity().findViewById<ViewGroup>(android.R.id.content), false)
        layout.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val toast = Toast(requireContext())
        toast.duration = Toast.LENGTH_LONG
        toast.view = layout
        toast.setGravity(Gravity.BOTTOM, 0, 200)
        toast.show()
    }

    private fun updateUi() {
        profile.nickname = binding.edtNickname.text.toString()
        val nickname = binding.edtNickname.text.toString().trim()
        binding.firstLetterTextView.text = if (nickname.isEmpty()) "" else nickname[0].toString()
        binding.edtNickname.error = if (nickname.length > 15) "Nickname is too long" else null
        binding.charCountTextView.text = getString(R.string.char_сountTextView, nickname.length)
    }

    private fun createTintList(enabledColor: Int, disabledColor: Int): ColorStateList {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_enabled), intArrayOf(-android.R.attr.state_enabled)
        )
        val colors = intArrayOf(enabledColor, disabledColor)
        return ColorStateList(states, colors)
    }

    override fun getTitleRes(): Int = R.string.edit_profile

    override fun getCustomAction(): CustomAction {
        return CustomAction(iconRes = R.drawable.ic_done,
            textRes = R.string.done,
            onCustomAction = { saveChanges() })
    }
}