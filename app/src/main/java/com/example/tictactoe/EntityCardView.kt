package com.example.tictactoe

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
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
    var ivAvatar: ImageView
    var avatarResId: Int = 0
        set(value) {
            field = value
            binding.ivAvatar.setImageResource(value)
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

        ivAvatar = binding.ivAvatar

        adjustTextSize()

        initializeAttributes(attrs, defStyleAttr, defStyleRes)
    }

    private fun adjustTextSize() {
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val textSizeInDp = screenWidth * 0.045f / Resources.getSystem().displayMetrics.density

        binding.tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSizeInDp)
        binding.tvDescription.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSizeInDp)
    }


    private fun initializeAttributes(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        if (attrs == null) {
            return
        }

        context.withStyledAttributes(
            attrs, R.styleable.EntityCardView, defStyleAttr, defStyleRes
        ) {
            name =
                getString(R.styleable.EntityCardView_name) ?: context.getString(R.string.easy_bot)
            avatarResId = getResourceId(R.styleable.EntityCardView_avatar, R.drawable.easy_bot)
            mark = getResourceId(R.styleable.EntityCardView_mark, R.drawable.tac)
            description = getString(R.styleable.EntityCardView_description) ?: ""
            active = getBoolean(R.styleable.EntityCardView_active, false)
        }
    }
}