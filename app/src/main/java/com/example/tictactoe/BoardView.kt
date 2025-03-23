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
import com.example.tictactoe.ai.Mark
import com.example.tictactoe.databinding.BoardBinding
import kotlin.properties.Delegates

typealias BoardViewListener = (r: Int, c: Int) -> Unit

class BoardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {
    private val binding: BoardBinding
    var humanMark: Int by Delegates.notNull<Int>()
    val listeners = mutableListOf<BoardViewListener>()

    init {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.board, this, true)
        binding = BoardBinding.bind(this)
        for (cell in getCells()) {
            cell.setOnClickListener{
                val index = getCells().indexOf(cell)
                val row = index / 3
                val col = index % 3
                updateCell(cell, humanMark)
                notifyChanges(row, col)
            }
        }
    }

    fun setMove(row: Int, column: Int, mark: Mark) {
        val cells = getCells()
        val twoDimArray = Array(3) { row -> cells.sliceArray(row * 3 until (row + 1) * 3) }
        val cell = twoDimArray[row][column]
        when (mark) {
            Mark.TAC -> updateCell(cell, TAC)
            Mark.TIC -> updateCell(cell, TIC)
            else -> return
        }
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

    fun addListener(listener: BoardViewListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: BoardViewListener) {
        listeners.remove(listener)
    }

    private fun notifyChanges(row: Int, col: Int) {
        listeners.forEach { it.invoke(row, col) }
    }


    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()!!
        val savedState = SavedState(superState)

        val stateViews = IntArray(9)
        val cells = getCells()

        for (i in stateViews.indices) {
            stateViews[i] = when (cells[i].tag) {
                TIC -> 1
                TAC -> -1
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
                1 -> updateCell(cells[i], TIC)
                -1 -> updateCell(cells[i], TAC)
                else -> continue
            }
        }
    }


    companion object {
        private val TIC = R.drawable.cell_tic
        private val TAC = R.drawable.cell_tac
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
