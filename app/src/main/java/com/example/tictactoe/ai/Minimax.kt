package com.example.tictactoe.ai

import kotlin.math.max
import kotlin.math.min

enum class Mark {
    TIC, TAC, EMPTY
}

class Minimax(private val board: Board, private val human: Mark, private val maxDepth: Int) {
    private val computer: Mark = if (human == Mark.TIC) Mark.TAC else Mark.TIC

    private fun evaluate(): Int {
        val lines = mutableListOf<Array<Mark>>().apply {
            addAll(board.getGrid())

            for (c in board.getGrid().indices) {
                add(Array(3) { r -> board[r, c] })
            }

            add(Array(3) { i -> board[i, i] })
            add(Array(3) { i -> board[i, board.getGrid().size - 1 - i] })
        }

        val computerLine = Array(3) { computer }
        val humanLine = Array(3) { human }

        return when {
            lines.any { it contentEquals humanLine } -> -1
            lines.any { it contentEquals computerLine } -> 1
            else -> 0
        }
    }

    private fun minimax(isMaximizing: Boolean, depth: Int): Int {
        val rating = evaluate()
        val emptyCells = board.getEmptyCells()

        when {
            rating == 1 || rating == -1 -> return rating
            emptyCells.isEmpty() || depth == maxDepth -> return 0
        }

        return if (isMaximizing) {
            var best = Int.MIN_VALUE

            for ((r, c) in emptyCells) {
                board[r, c] = computer
                best = max(best, minimax(false, depth + 1))
                board[r, c] = Mark.EMPTY
            }
            best
        } else {
            var best = Int.MAX_VALUE

            for ((r, c) in emptyCells) {
                board[r, c] = human
                best = min(best, minimax(true, depth + 1))
                board[r, c] = Mark.EMPTY
            }
            best
        }
    }

    fun findBestMove(): Pair<Int, Int> {
        var bestRating = Int.MIN_VALUE
        var bestMove = Pair(-1, -1)

        for ((r, c) in board.getEmptyCells()) {
            board[r, c] = computer
            val moveRating = minimax(false, 0)
            board[r, c] = Mark.EMPTY

            if (moveRating > bestRating) {
                bestRating = moveRating
                bestMove = Pair(r, c)
            }
        }

        return bestMove
    }
}