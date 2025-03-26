package com.example.tictactoe

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.drawable.toBitmap
import com.example.tictactoe.databinding.EntityCardBinding

class EntityCardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding: EntityCardBinding

    var name: String = ""
        set(value) {
            field = value
            binding.tvName.text = value
        }

    val ivAvatar: ImageView
        get() = binding.ivAvatar

    var avatarResId: Int = 0
        set(value) {
            field = value
            binding.ivAvatar.setImageResource(value)
            updateAvatarBitmapFromImageView()
        }

    var avatarBitmap: Bitmap? = null
        set(value) {
            field = value
            if (value != null) {
                binding.ivAvatar.setImageBitmap(value)
            }
        }

    var mark: Int = 0
        set(value) {
            field = value
            binding.ivMark.setImageResource(value)
        }

    var description: String = ""
        set(value) {
            field = value
            binding.tvDescription.text = value
        }

    var active = false
        set(value) {
            field = value
            binding.tvDescription.visibility = if (value) VISIBLE else GONE
            binding.clEntity.setBackgroundResource(if (value) R.drawable.bg_status_active else R.drawable.bg_status_inactive)
        }

    init {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.entity_card, this, true)
        binding = EntityCardBinding.bind(this)
        binding.ivAvatar.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateAvatarBitmapFromImageView()
        }

        adjustTextSize()

        initializeAttributes(attrs, defStyleAttr, defStyleRes)
    }

    fun loadEntityCard(entityCard: EntityCard) {
        name = entityCard.name
        avatarBitmap = entityCard.avatar
        mark = entityCard.mark
        description = entityCard.description
        invalidate()
    }

    private fun adjustTextSize() {
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val textSizeInDp = screenWidth * 0.045f / Resources.getSystem().displayMetrics.density

        binding.tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSizeInDp)
        binding.tvDescription.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSizeInDp)
    }

    private fun initializeAttributes(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        if (attrs == null) return

        context.withStyledAttributes(
            attrs, R.styleable.EntityCardView, defStyleAttr, defStyleRes
        ) {
            name = getString(R.styleable.EntityCardView_name) ?: context.getString(R.string.easy_bot)
            avatarResId = getResourceId(R.styleable.EntityCardView_avatar, R.drawable.easy_bot)
            mark = getResourceId(R.styleable.EntityCardView_mark, R.drawable.tac)
            description = getString(R.styleable.EntityCardView_description) ?: ""
            active = getBoolean(R.styleable.EntityCardView_active, false)
        }
    }

    private fun updateAvatarBitmapFromImageView() {
        val drawable = binding.ivAvatar.drawable
        avatarBitmap = if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else {
            drawable?.toBitmap()
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        savedState.entityCard = EntityCard(name, description, avatarBitmap, mark)
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)
        state.entityCard?.let {
            name = it.name
            description = it.description
            avatarBitmap = it.avatar
            mark = it.mark
        }
    }

    class SavedState : BaseSavedState {
        var entityCard: EntityCard? = null

        constructor(superState: Parcelable?) : super(superState)

        constructor(parcel: Parcel) : super(parcel) {
            entityCard = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                parcel.readParcelable(EntityCard::class.java.classLoader, EntityCard::class.java)
            } else {
                @Suppress("DEPRECATION") parcel.readParcelable(EntityCard::class.java.classLoader)
            }
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeParcelable(entityCard, flags)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState {
                    return SavedState(source)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }
}