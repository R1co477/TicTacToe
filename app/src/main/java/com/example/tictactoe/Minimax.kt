package com.example.tictactoe

enum class Mark {
    TIC, TAC
}

class Minimax(val board: Array<Mark>, val human: Mark, val depth: Int) {
    val computer: Mark = if (human == Mark.TIC) Mark.TAC else Mark.TIC
    private fun evaluate() {

    }

    private fun minimax(isMaximizing: Boolean) {

    }

    private fun getMove() {
        var bestRating = Int.MIN_VALUE
        val bestMove = arrayOf(-1, -1)

    }

}