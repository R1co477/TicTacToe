package com.example.tictactoe.utils

import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.tictactoe.Profile
import com.example.tictactoe.R

class AvatarManager(private val profile: Profile) {
    fun setAvatar(imageView: ImageView, textView: TextView) {
        if (profile.avatarUri == null) {
            textView.text = profile.nickname[0].toString()
            textView.visibility = TextView.VISIBLE
            imageView.background =
                ContextCompat.getDrawable(imageView.context, R.drawable.profile_avatar)
            imageView.backgroundTintList = profile.selectedColor
        } else {
            textView.visibility = TextView.INVISIBLE
            Glide.with(imageView.context).asDrawable().load(profile.avatarUri).circleCrop()
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable, transition: Transition<in Drawable>?
                    ) {
                        imageView.background = resource
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }
    }
}
