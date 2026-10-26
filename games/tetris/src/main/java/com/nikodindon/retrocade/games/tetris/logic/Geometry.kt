package com.nikodindon.retrocade.games.tetris.logic

const val BOARD_WIDTH = 10
const val VISIBLE_HEIGHT = 20
const val HIDDEN_ROWS = 2
const val BOARD_HEIGHT = VISIBLE_HEIGHT + HIDDEN_ROWS

data class Cell(val x: Int, val y: Int)

enum class PieceType(
    val colorIndex: Int,
    val spawnX: Int,
    val boxSize: Int,
    val baseCells: List<Cell>,
) {
    I(1, 3, 4, listOf(Cell(0, 1), Cell(1, 1), Cell(2, 1), Cell(3, 1))),
    O(2, 4, 2, listOf(Cell(0, 0), Cell(1, 0), Cell(0, 1), Cell(1, 1))),
    T(3, 3, 3, listOf(Cell(1, 0), Cell(0, 1), Cell(1, 1), Cell(2, 1))),
    S(4, 3, 3, listOf(Cell(1, 0), Cell(2, 0), Cell(0, 1), Cell(1, 1))),
    Z(5, 3, 3, listOf(Cell(0, 0), Cell(1, 0), Cell(1, 1), Cell(2, 1))),
    J(6, 3, 3, listOf(Cell(0, 0), Cell(0, 1), Cell(1, 1), Cell(2, 1))),
    L(7, 3, 3, listOf(Cell(2, 0), Cell(0, 1), Cell(1, 1), Cell(2, 1))),
    ;

    val spawnRotation: Int get() = 0
    val spawnY: Int get() = 0

    fun cells(rotation: Int): List<Cell> = PieceShapes.cells(this, rotation)
}

object PieceShapes {
    private val cache: Map<PieceType, List<List<Cell>>> = PieceType.entries.associateWith { type ->
        var current = type.baseCells
        buildList {
            repeat(4) {
                add(current.sortedWith(compareBy<Cell> { cell -> cell.y }.thenBy { it.x }))
                current = current.map { cell ->
                    Cell(type.boxSize - 1 - cell.y, cell.x)
                }
            }
        }
    }

    fun cells(type: PieceType, rotation: Int): List<Cell> =
        cache.getValue(type)[Math.floorMod(rotation, 4)]
}

data class Tetromino(
    val type: PieceType,
    val rotation: Int,
    val x: Int,
    val y: Int,
) {
    val cells: List<Cell> get() = type.cells(rotation)

    fun moved(dx: Int = 0, dy: Int = 0): Tetromino = copy(x = x + dx, y = y + dy)

    fun rotated(direction: Int): Tetromino =
        copy(rotation = Math.floorMod(rotation + direction, 4))

    companion object {
        fun spawn(type: PieceType): Tetromino =
            Tetromino(type, type.spawnRotation, type.spawnX, type.spawnY)
    }
}
