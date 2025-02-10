package com.example.tictactoe

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.tictactoe.databinding.BoardBinding

class BoardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {
    private val binding: BoardBinding
    init {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.board, this, true)
        binding = BoardBinding.bind(this)
        var isTic = true
        val drawableTic = R.drawable.cell_tic
        val drawableTac = R.drawable.cell_tac
        for (view in getViews()) {
            val drawable = if (isTic) {
                drawableTic
            } else {
                drawableTac
            }
            if (isTic) {
                isTic = false
            } else {
                isTic = true
            }
            view.setOnClickListener {
                updateCell(view, drawable)
                view.isClickable = false
            }
        }
    }
    private fun setMove() {

    }

    private fun getViews(): Array<View> {
        with(binding) {
            return arrayOf(cell11, cell12, cell13, cell21, cell22, cell22, cell23, cell31, cell32, cell33)
        }
    }


    private fun updateCell(view: View, newDrawableRes: Int) {
        val context = view.context
        val halfDuration = 150L

        val flipOut = ObjectAnimator.ofFloat(view, "rotationY", 0f, 90f).apply {
            duration = halfDuration
            interpolator = AccelerateInterpolator()
        }

        flipOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                view.background = ContextCompat.getDrawable(context, newDrawableRes)
                view.rotationY = -90f

                val flipIn = ObjectAnimator.ofFloat(view, "rotationY", -90f, 0f).apply {
                    duration = halfDuration
                    interpolator = DecelerateInterpolator()
                }
                flipIn.start()
            }
        })
        flipOut.start()
    }
}