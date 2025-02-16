package com.example.tictactoe

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context

import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
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
        for (cell in getCells()) {
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
            cell.setOnClickListener {
                updateCell(cell, drawable)
            }
        }
    }

    private fun setMove() {

    }

    private fun getCells(): Array<ImageView> {
        with(binding) {
            return arrayOf(
                cell11, cell12, cell13, cell21, cell22, cell23, cell31, cell32, cell33
            )
        }
    }


    private fun updateCell(imageView: ImageView, newDrawableRes: Int) {
        imageView.tag = newDrawableRes
        imageView.isClickable = false
        val halfDuration = 150L

        val flipOut = ObjectAnimator.ofFloat(imageView, "rotationY", 0f, 90f).apply {
            duration = halfDuration
            interpolator = AccelerateInterpolator()
        }

        flipOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                imageView.setImageResource(newDrawableRes)
                imageView.rotationY = -90f

                val flipIn = ObjectAnimator.ofFloat(imageView, "rotationY", -90f, 0f).apply {
                    duration = halfDuration
                    interpolator = DecelerateInterpolator()
                }
                flipIn.start()
            }
        })
        flipOut.start()
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()!!
        val savedState = SavedState(superState)

        val stateViews: IntArray = IntArray(9)
        val cells = getCells()

        for (i in stateViews.indices) {
            stateViews[i] = when (cells[i].tag) {
                R.drawable.cell_tic -> 1
                R.drawable.cell_tac -> -1
                else -> 0
            }

        }
        savedState.stateViews = stateViews
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)
        val cells = getCells()
        val stateViews = savedState.stateViews
        for (i in cells.indices) {
            when (stateViews[i]) {
                1 -> updateCell(cells[i], R.drawable.cell_tic)
                -1 -> updateCell(cells[i], R.drawable.cell_tac)
                else -> continue
            }
        }
    }

    class SavedState : BaseSavedState {
        var stateViews: IntArray = IntArray(9)

        constructor(superState: Parcelable) : super(superState)

        constructor(parcel: Parcel) : super(parcel) {
            stateViews = parcel.createIntArray() ?: IntArray(9)
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeIntArray(stateViews)
        }

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(parcel: Parcel): SavedState {
                    return SavedState(parcel)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }
}
