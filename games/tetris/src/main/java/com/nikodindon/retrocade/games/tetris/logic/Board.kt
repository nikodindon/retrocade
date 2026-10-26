package com.nikodindon.retrocade.games.tetris.logic

/**
 * 10×22 board. Rows 0 and 1 are hidden above the visible playfield and exist only
 * so rotations and spawns can happen outside the player's view.
 */
class Board(
    private val cells: IntArray = IntArray(BOARD_WIDTH * BOARD_HEIGHT),
) {
    fun get(x: Int, y: Int): Int {
        if (x !in 0 until BOARD_WIDTH || y !in 0 until BOARD_HEIGHT) return -1
        return cells[index(x, y)]
    }

    fun set(x: Int, y: Int, value: Int) {
        if (x in 0 until BOARD_WIDTH && y in 0 until BOARD_HEIGHT) {
            cells[index(x, y)] = value
        }
    }

    fun clear() {
        cells.fill(0)
    }

    fun collides(type: PieceType, rotation: Int, x: Int, y: Int): Boolean =
        collides(type.cells(rotation), x, y)

    fun collides(cellsToCheck: List<Cell>, x: Int, y: Int): Boolean {
        for (cell in cellsToCheck) {
            val boardX = x + cell.x
            val boardY = y + cell.y
            if (boardX !in 0 until BOARD_WIDTH || boardY < 0 || boardY >= BOARD_HEIGHT) return true
            if (cells[index(boardX, boardY)] != 0) return true
        }
        return false
    }

    fun collides(piece: Tetromino): Boolean = collides(piece.cells, piece.x, piece.y)

    fun grounded(piece: Tetromino): Boolean =
        collides(piece.cells, piece.x, piece.y + 1)

    fun merge(piece: Tetromino) {
        piece.cells.forEach { cell ->
            val x = piece.x + cell.x
            val y = piece.y + cell.y
            if (x in 0 until BOARD_WIDTH && y in 0 until BOARD_HEIGHT) {
                cells[index(x, y)] = piece.type.colorIndex
            }
        }
    }

    fun fullRows(): List<Int> {
        val rows = ArrayList<Int>(4)
        for (y in 0 until BOARD_HEIGHT) {
            var full = true
            for (x in 0 until BOARD_WIDTH) {
                if (cells[index(x, y)] == 0) {
                    full = false
                    break
                }
            }
            if (full) rows += y
        }
        return rows
    }

    /** Removes [rows] and lets everything above fall down. Returns the cleared count. */
    fun clearRows(rows: List<Int>): Int {
        if (rows.isEmpty()) return 0
        val toClear = rows.toHashSet()
        var write = BOARD_HEIGHT - 1
        for (read in BOARD_HEIGHT - 1 downTo 0) {
            if (read !in toClear) {
                if (write != read) {
                    for (x in 0 until BOARD_WIDTH) {
                        cells[index(x, write)] = cells[index(x, read)]
                    }
                }
                write -= 1
            }
        }
        while (write >= 0) {
            for (x in 0 until BOARD_WIDTH) {
                cells[index(x, write)] = 0
            }
            write -= 1
        }
        return toClear.size
    }

    fun filledCellCount(): Int = cells.count { it != 0 }

    fun copyCells(): IntArray = cells.copyOf()

    private fun index(x: Int, y: Int): Int = y * BOARD_WIDTH + x

    companion object {
        /** Builds a board from 22 strings of 10 characters ('.' empty, other filled). */
        fun fromRows(rows: List<String>): Board {
            require(rows.size == BOARD_HEIGHT) { "expected $BOARD_HEIGHT rows" }
            val board = Board()
            rows.forEachIndexed { y, row ->
                require(row.length == BOARD_WIDTH) { "row $y must have $BOARD_WIDTH columns" }
                row.forEachIndexed { x, char ->
                    if (char != '.') board.set(x, y, 1)
                }
            }
            return board
        }
    }
}
