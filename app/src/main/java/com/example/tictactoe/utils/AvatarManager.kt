package com.example.tictactoe.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.tictactoe.Profile
import com.example.tictactoe.R
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable

class AvatarManager(private val profile: Profile) {
//    fun setAvatar(imageView: ImageView, textView: TextView) {
//        if (profile.avatarUri == null) {
//            textView.text = profile.nickname[0].toString()
//            textView.visibility = TextView.VISIBLE
//            imageView.background =
//                ContextCompat.getDrawable(imageView.context, R.drawable.profile_avatar)
//            imageView.backgroundTintList = profile.selectedColor
//        } else {
//            textView.visibility = TextView.INVISIBLE
//            Glide.with(imageView.context).asDrawable().load(profile.avatarUri).circleCrop()
//                .into(object : CustomTarget<Drawable>() {
//                    override fun onResourceReady(
//                        resource: Drawable, transition: Transition<in Drawable>?
//                    ) {
//                        imageView.background = resource
//                    }
//
//                    override fun onLoadCleared(placeholder: Drawable?) {}
//                })
//        }
//    }
    fun setAvatar(imageView: ImageView) {
        if (profile.avatarUri == null) {
            val textDrawable = createTextDrawable(
                profile.nickname[0].toString(), 
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
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                        imageView.setImageDrawable(resource)
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }
        }
    }
    
    private fun createTextDrawable(letter: String, color: Int, imageView: ImageView): Drawable {
        val size = if (imageView.width > 0) imageView.width else 200
        
        val bitmap = createBitmap(size, size)
        val canvas = Canvas(bitmap)
        
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = color
        canvas.drawCircle(size/2f, size/2f, size/2f, paint)
        
        paint.color = Color.WHITE
        paint.textSize = size * 0.4f
        paint.textAlign = Paint.Align.CENTER
        
        val textHeight = paint.descent() - paint.ascent()
        val textOffset = textHeight / 2 - paint.descent()
        
        canvas.drawText(letter, size/2f, size/2f + textOffset, paint)
        
        return bitmap.toDrawable(imageView.context.resources)
    }
