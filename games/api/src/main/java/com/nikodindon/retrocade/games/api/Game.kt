package com.nikodindon.retrocade.games.api

import androidx.compose.ui.graphics.drawscope.DrawScope
import com.nikodindon.retrocade.core.common.ControlScheme
import com.nikodindon.retrocade.core.common.ProceduralIcon
import kotlinx.coroutines.flow.StateFlow

/**
 * A playable game. The app composes games through Hilt and never needs to know
 * their implementation details.
 */
interface Game {
    val meta: GameMeta

    fun createSession(seed: Long, settings: GameSettings): GameSession
}

interface GameSession {
    val state: StateFlow<GameStatus>

    /** Advances the simulation by a fixed step. Never touches Android or the UI. */
    fun update(dtMillis: Long)

    fun onInput(event: InputEvent)

    fun render(canvas: DrawScope)
}

data class GameMeta(
    val id: String,
    val name: String,
    val tagline: String,
    val description: String,
    val icon: ProceduralIcon,
    val accentColor: Long,
    val releaseLabel: String,
)

data class GameSettings(
    val controlScheme: ControlScheme = ControlScheme.TAP,
    val swipeThresholdDp: Float = 24f,
    val hapticsEnabled: Boolean = true,
)

sealed interface GameStatus {
    data object Playing : GameStatus

    data object Paused : GameStatus

    data class GameOver(val score: Int) : GameStatus
}

sealed interface InputEvent {
    /** Press or release of a horizontal control. Direction is -1 (left) or +1 (right). */
    data class HorizontalHold(val direction: Int, val pressed: Boolean) : InputEvent

    data object RotateClockwise : InputEvent

    data object RotateCounterClockwise : InputEvent

    data object HardDrop : InputEvent

    data class SoftDrop(val pressed: Boolean) : InputEvent

    /** Tetris-style hold; ignored by games that do not use it. */
    data object HoldPiece : InputEvent

    data object TogglePause : InputEvent
}
