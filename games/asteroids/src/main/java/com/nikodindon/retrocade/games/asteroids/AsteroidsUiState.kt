package com.nikodindon.retrocade.games.asteroids

import com.nikodindon.retrocade.games.api.GameSettings
import com.nikodindon.retrocade.games.api.GameStatus

data class AsteroidsUiState(
    val status: GameStatus = GameStatus.Playing,
    val score: Int = 0,
    val lives: Int = 3,
    val wave: Int = 1,
    val settings: GameSettings = GameSettings(),
)
