package com.nikodindon.retrocade.games.tetris.logic

sealed interface TetrisEvent {
    data class Moved(val direction: Int) : TetrisEvent

    data object Rotated : TetrisEvent

    data object PieceLocked : TetrisEvent

    data class HardDropped(val cells: Int) : TetrisEvent

    data class LineCleared(
        val lines: Int,
        val rows: List<Int>,
        val combo: Int,
        val backToBack: Boolean,
    ) : TetrisEvent

    data class LevelUp(val level: Int) : TetrisEvent

    data object HoldUsed : TetrisEvent

    data class GameOver(val score: Int) : TetrisEvent
}
