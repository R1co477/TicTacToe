package com.example.tictactoe.ai

import kotlin.math.max
import kotlin.math.min

enum class Mark {
    TIC, TAC, EMPTY
}

class Minimax(private val board: Board, private val human: Mark, private val maxDepth: Int) {
    private val computer: Mark = if (human == Mark.TIC) Mark.TAC else Mark.TIC

    private fun evaluate(): Int {
        for (row in board.getGrid()) {
            if (row[0] != Mark.EMPTY && row[0] == row[1] && row[1] == row[2]) {
                return if (row[0] == computer) 10 else -10
            }
        }

        for (c in 0 until board.getGrid().size) {
            if (board[0, c] != Mark.EMPTY &&
                board[0, c] == board[1, c] &&
                board[1, c] == board[2, c]
            ) {
                return if (board[0, c] == computer) 10 else -10
            }
        }

        if (board[0, 0] != Mark.EMPTY &&
            board[0, 0] == board[1, 1] &&
            board[1, 1] == board[2, 2]
        ) {
            return if (board[0, 0] == computer) 10 else -10
        }

        if (board[0, 2] != Mark.EMPTY &&
            board[0, 2] == board[1, 1] &&
            board[1, 1] == board[2, 0]
        ) {
            return if (board[0, 2] == computer) 10 else -10
        }

        return 0
    }

    private fun minimax(isMaximizing: Boolean, depth: Int): Int {
        val score = evaluate()

        if (score == 10) return score - depth
        if (score == -10) return score + depth
        if (board.getEmptyCells().isEmpty() || depth >= maxDepth) return 0

        if (isMaximizing) {
            var best = Int.MIN_VALUE

            for ((r, c) in board.getEmptyCells()) {
                board[r, c] = computer
                best = max(best, minimax(false, depth + 1))
                board[r, c] = Mark.EMPTY
            }
            return best
        } else {
            var best = Int.MAX_VALUE

            for ((r, c) in board.getEmptyCells()) {
                board[r, c] = human
                best = min(best, minimax(true, depth + 1))
                board[r, c] = Mark.EMPTY
            }
            return best
        }
    }

    fun findBestMove(): Pair<Int, Int> {
        var bestVal = Int.MIN_VALUE
        var bestMove = Pair(-1, -1)

        for ((r, c) in board.getEmptyCells()) {
            board[r, c] = computer
            val moveVal = minimax(false, 0)
            board[r, c] = Mark.EMPTY


            if (moveVal > bestVal) {
                bestMove = Pair(r, c)
                bestVal = moveVal
            }
        }

        return bestMove
    }
}