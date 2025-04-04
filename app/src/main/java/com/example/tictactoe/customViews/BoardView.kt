package com.example.tictactoe.customViews

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.tictactoe.R
import com.example.tictactoe.ai.Mark
import com.example.tictactoe.databinding.BoardBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

enum class GameState {
    ONGOING,
    HUMAN_WIN,
    AI_WIN,
    DRAW
}

typealias BoardViewListener = (r: Int, c: Int) -> Unit
typealias GameStateListener = (GameState) -> Unit

class BoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding: BoardBinding
    private val cells: Array<ImageView>

    var humanMark: Int by Delegates.notNull<Int>()
    private var boardState = Array(3) { IntArray(3) { 0 } }

    private val moveListeners = mutableListOf<BoardViewListener>()
    private val gameStateListeners = mutableListOf<GameStateListener>()

    internal var currentGameState: GameState = GameState.ONGOING
        set(value) {
            if (field != value) {
                field = value
                notifyGameStateChanged(value)
            }
        }

    init {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.board, this, true)
        binding = BoardBinding.bind(this)
        cells = getCells()

        setupCellClickListeners()
    }

    private fun setupCellClickListeners() {
        for (cell in cells) {
            cell.setOnClickListener {
                val index = cells.indexOf(it)
                val row = index / 3
                val col = index % 3

                if (boardState[row][col] == 0 && currentGameState == GameState.ONGOING) {
                    makeHumanMove(row, col)
                }
            }
        }
    }

    private fun makeHumanMove(row: Int, col: Int) {
        notifyChanges(row, col)
        checkGameState()
    }

    fun setMove(row: Int, column: Int, mark: Mark) {
        val cell = cells[row * 3 + column]
        when (mark) {
            Mark.TAC -> updateCell(cell, TAC)
            Mark.TIC -> updateCell(cell, TIC)
            else -> return
        }
        boardState[row][column] = if (mark == Mark.TIC) 1 else -1
        checkGameState()
    }

    private fun checkGameState() {
        val humanValue = if (humanMark == TIC) 1 else -1
        val aiValue = if (humanMark == TIC) -1 else 1

        if (checkWin(humanValue)) {
            currentGameState = GameState.HUMAN_WIN
        } else if (checkWin(aiValue)) {
            currentGameState = GameState.AI_WIN
        } else if (isBoardFull()) {
            currentGameState = GameState.DRAW
        } else {
            currentGameState = GameState.ONGOING
        }
    }

    private fun checkWin(player: Int): Boolean {
        for (row in 0 until 3) {
            if (boardState[row].all { it == player }) return true
        }

        for (col in 0 until 3) {
            if ((0 until 3).all { boardState[it][col] == player }) return true
        }

        if ((0 until 3).all { boardState[it][it] == player }) return true
        if ((0 until 3).all { boardState[it][2 - it] == player }) return true

        return false
    }

    private fun isBoardFull(): Boolean {
        return boardState.all { row -> row.all { it != 0 } }
    }

    fun addMoveListener(listener: BoardViewListener) {
        moveListeners.add(listener)
    }

    fun addGameStateListener(listener: GameStateListener) {
        gameStateListeners.add(listener)
    }

    private fun notifyChanges(row: Int, col: Int) {
        moveListeners.forEach { it.invoke(row, col) }
    }

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private fun notifyGameStateChanged(state: GameState) {
        scope.launch {
            when (state) {
                GameState.HUMAN_WIN -> {
                    val winLine = getWinLine(humanMark)
                    animateWinLine(winLine)
                    delay(1000)
                }

                GameState.AI_WIN -> {
                    val computerMark = if (humanMark == TIC) TAC else TIC
                    val winLine = getWinLine(computerMark)
                    animateWinLine(winLine)
                    delay(1000)
                }

                GameState.DRAW -> {
                    delay(1000)
                }

                GameState.ONGOING -> {

                }
            }
            gameStateListeners.forEach { it.invoke(state) }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        scope.cancel()
    }

    private fun getWinLine(markResId: Int): Array<ImageView> {
        val boardValue = if (markResId == TIC) 1 else -1

        for (i in 0 until 3) {
            if (boardState[0][i] == boardValue && boardState[1][i] == boardValue && boardState[2][i] == boardValue) {
                return arrayOf(getCells()[i], getCells()[i + 3], getCells()[i + 6])
            }
            if (boardState[i][0] == boardValue && boardState[i][1] == boardValue && boardState[i][2] == boardValue) {
                return arrayOf(getCells()[i * 3], getCells()[i * 3 + 1], getCells()[i * 3 + 2])
            }
        }

        if (boardState[0][0] == boardValue && boardState[1][1] == boardValue && boardState[2][2] == boardValue) {
            return arrayOf(getCells()[0], getCells()[4], getCells()[8])
        }
        if (boardState[0][2] == boardValue && boardState[1][1] == boardValue && boardState[2][0] == boardValue) {
            return arrayOf(getCells()[2], getCells()[4], getCells()[6])
        }

        return emptyArray()
    }

    private fun animateWinLine(winLine: Array<ImageView>) {
        if (winLine.isEmpty()) return

        for (cell in winLine) {
            cell.alpha = 1f
            cell.scaleX = 1f
            cell.scaleY = 1f
        }

        val duration = 300L
        val delay = 100L

        for (i in winLine.indices) {
            val cell = winLine[i]

            val scaleUp = ObjectAnimator.ofPropertyValuesHolder(
                cell,
                PropertyValuesHolder.ofFloat("scaleX", 1.2f),
                PropertyValuesHolder.ofFloat("scaleY", 1.2f)
            ).apply {
                this.duration = duration
                this.startDelay = i * delay
                interpolator = DecelerateInterpolator()
            }

            val scaleDown = ObjectAnimator.ofPropertyValuesHolder(
                cell,
                PropertyValuesHolder.ofFloat("scaleX", 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1f)
            ).apply {
                this.duration = duration
                interpolator = AccelerateInterpolator()
            }

            val highlight = ObjectAnimator.ofArgb(
                cell,
                "colorFilter",
                Color.WHITE,
                Color.YELLOW
            ).apply {
                this.duration = duration * 2
                this.startDelay = i * delay
                interpolator = AccelerateDecelerateInterpolator()
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ObjectAnimator.REVERSE
            }

            val animatorSet = AnimatorSet().apply {
                playSequentially(scaleUp, scaleDown)
            }

            animatorSet.start()
            highlight.start()
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()!!
        val savedState = SavedState(superState)

        savedState.boardState = boardState.flatMap { it.toList() }.toIntArray()
        savedState.gameState = currentGameState.ordinal

        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)

        val flatState = savedState.boardState
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                boardState[i][j] = flatState[i * 3 + j]
                when (flatState[i * 3 + j]) {
                    1 -> updateCell(cells[i * 3 + j], TIC)
                    -1 -> updateCell(cells[i * 3 + j], TAC)
                }
            }
        }

        currentGameState = GameState.entries.toTypedArray()[savedState.gameState]
    }

    private fun getCells(): Array<ImageView> {
        with(binding) {
            return arrayOf(
                cell11, cell12, cell13,
                cell21, cell22, cell23,
                cell31, cell32, cell33
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

    companion object {
        private val TIC = R.drawable.cell_tic
        private val TAC = R.drawable.cell_tac
    }

    class SavedState : BaseSavedState {
        var boardState: IntArray = IntArray(9)
        var gameState: Int = 0

        constructor(superState: Parcelable) : super(superState)

        constructor(parcel: Parcel) : super(parcel) {
            boardState = parcel.createIntArray() ?: IntArray(9)
            gameState = parcel.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeIntArray(boardState)
            out.writeInt(gameState)
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