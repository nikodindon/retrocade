package com.nikodindon.retrocade.games.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.nikodindon.retrocade.core.audio.AudioController
import com.nikodindon.retrocade.core.common.ControlScheme
import com.nikodindon.retrocade.core.common.ProceduralIcon
import kotlinx.coroutines.flow.StateFlow

/**
 * A playable game. The app composes games through Hilt and never needs to know
 * their implementation details: adding a game only touches its own module.
 */
interface Game {
    val meta: GameMeta

    fun createSession(seed: Long, settings: GameSettings): GameSession

    /**
     * Draws the game screen. Implementations own their HUD and gestures, while the
     * shared route owns settings, scores, ads and navigation.
     */
    @Composable
    fun GameScreen(context: GameUiContext)
}

interface GameSession {
    val state: StateFlow<GameStatus>

    /** Advances the simulation by a fixed step. Never touches Android or the UI. */
    fun update(dtMillis: Long)

    fun onInput(event: InputEvent)

    fun render(canvas: DrawScope)
}

data class GameMeta(
    /** Stable identifier used by navigation, storage and analytics. */
    val id: String,
    val name: String,
    /** Short label shown in the hub card. */
    val tagline: String,
    val description: String,
    /** Stable category identifier, for example "puzzle" or "shooter". */
    val categoryId: String,
    /** Pixel matrix painted on Canvas; no bitmap asset is involved. */
    val icon: ProceduralIcon,
    val accentColor: Long,
    val releaseLabel: String,
)

data class GameSettings(
    val controlScheme: ControlScheme = ControlScheme.TAP,
    val swipeThresholdDp: Float = 28f,
    val hapticsEnabled: Boolean = true,
    val sfxEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    /** Hint for games that expose a secondary slot in their HUD (Tetris hold). */
    val showHoldButton: Boolean = true,
)

/** Everything a game screen needs from the host application. */
data class GameUiContext(
    val session: GameSession,
    val settings: GameSettings,
    val bestScore: Int,
    val audio: AudioController,
    val onExit: () -> Unit,
    val onReplay: () -> Unit,
    val onOpenPaywall: () -> Unit,
    val onRecordScore: suspend (score: Int, lines: Int, level: Int) -> GameScoreResult,
)

data class GameScoreResult(
    val isNewBest: Boolean,
    val best: Int,
    val rank: Int?,
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
