package com.nikodindon.retrocade.games.tetris

import com.nikodindon.retrocade.games.api.GameSettings
import com.nikodindon.retrocade.games.api.GameStatus
import com.nikodindon.retrocade.games.tetris.logic.PieceType

data class TetrisUiState(
    val status: GameStatus = GameStatus.Playing,
    val score: Int = 0,
    val lines: Int = 0,
    val level: Int = 1,
    val combo: Int = -1,
    val backToBack: Boolean = false,
    val next: List<PieceType> = emptyList(),
    val hold: PieceType? = null,
    val canHold: Boolean = true,
    val settings: GameSettings = GameSettings(),
)
