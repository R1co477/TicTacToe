package com.example.tictactoe.ai


@JvmInline
value class Board(private val grid: Array<Array<Mark>>) {

    operator fun get(row: Int, col: Int): Mark = grid[row][col]

    operator fun set(row: Int, col: Int, mark: Mark) {
        if (row !in 0 until 3 || col !in 0 until 3) {
            throw IllegalArgumentException("Invalid row or column index")
        }
        grid[row][col] = mark
    }

    fun getEmptyCells(): MutableList<Pair<Int, Int>> {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (r in grid.indices) {
            for (c in grid[r].indices) {
                if (grid[r][c] == Mark.EMPTY) {
                    emptyCells.add(Pair(r, c))
                }
            }
        }
        return emptyCells
    }

    fun getGrid(): Array<Array<Mark>> = grid

    companion object {
        fun empty(): Board = Board(Array(3) { Array(3) { Mark.EMPTY } })
    }
}