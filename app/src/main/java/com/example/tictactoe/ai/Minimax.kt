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

        for (line in lines) {
            if (line[0] != Mark.EMPTY && line[0] == line[1] && line[1] == line[2]) {
                return if (line[0] == computer) 10 else -10
            }
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