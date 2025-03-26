package com.example.tictactoe.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.tictactoe.Profile

class AvatarManager(private val profile: Profile) {
    fun setAvatar(imageView: ImageView) {
        if (profile.avatarUri == null) {
            val nickname = profile.nickname
            val letter: String
            if (nickname.isEmpty()) {
                letter = " "
            } else {
                letter = nickname[0].toString()
            }
            val textDrawable = createTextDrawable(
                letter,
                profile.selectedColor?.defaultColor ?: Color.RED,
                imageView
            )
            imageView.setImageDrawable(textDrawable)
        } else {
            Glide.with(imageView.context)
                .asDrawable()
                .load(profile.avatarUri)
                .circleCrop()
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        imageView.setImageDrawable(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }
    }

    private fun createTextDrawable(letter: String, color: Int, imageView: ImageView): Drawable {
        val size = if (imageView.width > 0) imageView.width else 200

        val bitmap = createBitmap(size, size)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = color
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        paint.color = Color.WHITE
        paint.textSize = size * 0.4f
        paint.textAlign = Paint.Align.CENTER

        val textHeight = paint.descent() - paint.ascent()
        val textOffset = textHeight / 2 - paint.descent()

        canvas.drawText(letter, size / 2f, size / 2f + textOffset, paint)

        return bitmap.toDrawable(imageView.context.resources)
    }

    fun createTextBitmap(profile: Profile, context: Context): Bitmap {
        val size = 200
        val bitmap = createBitmap(size, size)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = profile.selectedColor?.defaultColor ?: Color.GRAY
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        val letter = profile.nickname.firstOrNull()?.uppercase() ?: "?"

        paint.color = Color.WHITE
        paint.textSize = size * 0.4f
        paint.textAlign = Paint.Align.CENTER

        val textHeight = paint.descent() - paint.ascent()
        val textOffset = textHeight / 2 - paint.descent()

        canvas.drawText(letter, size / 2f, size / 2f + textOffset, paint)

        return bitmap
    }

}
