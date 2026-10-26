package com.nikodindon.retrocade.games.tetris.logic

/**
 * Standard SRS wall kicks.
 *
 * Offsets are stored in the Tetris wiki convention (x to the right, y upwards)
 * to make auditing against the reference tables easy. [offset] converts the y
 * component to the internal convention (y downwards).
 */
object SrsKicks {
    data class Kick(val dx: Int, val dyUp: Int)

    private val jlstz: Map<Pair<Int, Int>, List<Kick>> = mapOf(
        (0 to 1) to listOf(Kick(0, 0), Kick(-1, 0), Kick(-1, 1), Kick(0, -2), Kick(-1, -2)),
        (1 to 0) to listOf(Kick(0, 0), Kick(1, 0), Kick(1, -1), Kick(0, 2), Kick(1, 2)),
        (1 to 2) to listOf(Kick(0, 0), Kick(1, 0), Kick(1, -1), Kick(0, 2), Kick(1, 2)),
        (2 to 1) to listOf(Kick(0, 0), Kick(-1, 0), Kick(-1, 1), Kick(0, -2), Kick(-1, -2)),
        (2 to 3) to listOf(Kick(0, 0), Kick(1, 0), Kick(1, 1), Kick(0, -2), Kick(1, -2)),
        (3 to 2) to listOf(Kick(0, 0), Kick(-1, 0), Kick(-1, -1), Kick(0, 2), Kick(-1, 2)),
        (3 to 0) to listOf(Kick(0, 0), Kick(-1, 0), Kick(-1, -1), Kick(0, 2), Kick(-1, 2)),
        (0 to 3) to listOf(Kick(0, 0), Kick(1, 0), Kick(1, 1), Kick(0, -2), Kick(1, -2)),
    )

    private val iKicks: Map<Pair<Int, Int>, List<Kick>> = mapOf(
        (0 to 1) to listOf(Kick(0, 0), Kick(-2, 0), Kick(1, 0), Kick(-2, -1), Kick(1, 2)),
        (1 to 0) to listOf(Kick(0, 0), Kick(2, 0), Kick(-1, 0), Kick(2, 1), Kick(-1, -2)),
        (1 to 2) to listOf(Kick(0, 0), Kick(-1, 0), Kick(2, 0), Kick(-1, 2), Kick(2, -1)),
        (2 to 1) to listOf(Kick(0, 0), Kick(1, 0), Kick(-2, 0), Kick(1, -2), Kick(-2, 1)),
        (2 to 3) to listOf(Kick(0, 0), Kick(2, 0), Kick(-1, 0), Kick(2, 1), Kick(-1, -2)),
        (3 to 2) to listOf(Kick(0, 0), Kick(-2, 0), Kick(1, 0), Kick(-2, -1), Kick(1, 2)),
        (3 to 0) to listOf(Kick(0, 0), Kick(1, 0), Kick(-2, 0), Kick(1, -2), Kick(-2, 1)),
        (0 to 3) to listOf(Kick(0, 0), Kick(-1, 0), Kick(2, 0), Kick(-1, 2), Kick(2, -1)),
    )

    fun candidates(type: PieceType, from: Int, target: Int): List<Kick> {
        if (type == PieceType.O) return listOf(Kick(0, 0))
        val table = if (type == PieceType.I) iKicks else jlstz
        return table[from to target] ?: listOf(Kick(0, 0))
    }

    /** Internal offset: x, y with y growing downwards. */
    fun offset(kick: Kick): Pair<Int, Int> = kick.dx to -kick.dyUp
}
