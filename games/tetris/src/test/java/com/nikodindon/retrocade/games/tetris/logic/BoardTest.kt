package com.nikodindon.retrocade.games.tetris.logic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BoardTest {
    @Test
    fun `rejects walls and floor`() {
        val board = Board()

        assertFalse(board.collides(PieceType.T, 0, 7, 2))
        assertTrue(board.collides(PieceType.T, 0, 8, 2))
        assertTrue(board.collides(PieceType.T, 0, -1, 2))
        assertFalse(board.collides(PieceType.O, 0, 4, 20))
        assertTrue(board.collides(PieceType.O, 0, 4, 21))
        assertFalse(board.collides(PieceType.I, 1, 3, 0))
    }

    @Test
    fun `rejects occupied cells`() {
        val board = Board()
        board.set(5, 10, 3)

        assertTrue(board.collides(PieceType.O, 0, 4, 9))
        assertFalse(board.collides(PieceType.O, 0, 4, 11))
    }

    @Test
    fun `grounded is true one cell above a surface`() {
        val board = Board()
        board.set(5, 21, 1)
        val piece = Tetromino(PieceType.O, 0, 4, 19)

        assertFalse(board.grounded(piece.moved(dy = -1)))
        assertTrue(board.grounded(piece))
    }

    @Test
    fun `merge writes piece color to the board`() {
        val board = Board()
        board.merge(Tetromino(PieceType.T, 0, 3, 20))

        assertEquals(4, board.filledCellCount())
        assertEquals(PieceType.T.colorIndex, board.get(4, 20))
        assertEquals(PieceType.T.colorIndex, board.get(3, 21))
    }

    @Test
    fun `detects one to four complete rows`() {
        listOf(1, 2, 3, 4).forEach { count ->
            val rows = MutableList(BOARD_HEIGHT) { ".........." }
            for (i in 0 until count) {
                rows[BOARD_HEIGHT - 1 - i] = "XXXXXXXXXX"
            }
            val board = Board.fromRows(rows)
            assertEquals(count, board.fullRows().size)
        }
    }

    @Test
    fun `clears one row and shifts everything down`() {
        val rows = MutableList(BOARD_HEIGHT) { ".........." }
        rows[20] = "X........."
        rows[21] = "XXXXXXXXXX"
        val board = Board.fromRows(rows)

        assertEquals(1, board.clearRows(listOf(21)))

        assertEquals(0, board.get(0, 20))
        assertEquals(1, board.get(0, 21))
        assertEquals(1, board.filledCellCount())
    }

    @Test
    fun `clears two four rows at once`() {
        val rows = MutableList(BOARD_HEIGHT) { ".........." }
        rows[20] = "X........."
        rows[21] = "XXXXXXXXXX"
        rows[19] = "X........."
        rows[18] = "XXXXXXXXXX"
        val board = Board.fromRows(rows)

        assertEquals(2, board.clearRows(listOf(18, 21)))

        assertEquals(2, board.filledCellCount())
        assertEquals(1, board.get(0, 20))
        assertEquals(1, board.get(0, 21))
    }

    @Test
    fun `clearing does not leave rows above hidden area`() {
        val rows = MutableList(BOARD_HEIGHT) { ".........." }
        repeat(8) { index -> rows[14 + index] = "XXXXXXXXXX" }
        val board = Board.fromRows(rows)

        assertEquals(8, board.clearRows((14..21).toList()))
        assertEquals(0, board.filledCellCount())
    }

    @Test
    fun `fromRows rejects wrong dimensions`() {
        assertTrue(
            runCatching { Board.fromRows(listOf("..........")) }.isFailure,
        )
    }
}
