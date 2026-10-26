package com.nikodindon.retrocade.games.tetris

import androidx.compose.ui.graphics.drawscope.DrawScope
import com.nikodindon.retrocade.games.api.GameSession
import com.nikodindon.retrocade.games.api.GameSettings
import com.nikodindon.retrocade.games.api.GameStatus
import com.nikodindon.retrocade.games.api.InputEvent
import com.nikodindon.retrocade.games.tetris.logic.TetrisEngine
import com.nikodindon.retrocade.games.tetris.logic.TetrisEvent
import com.nikodindon.retrocade.games.tetris.procedural.TetrisRenderer
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class TetrisSession(
    val seed: Long,
    val settings: GameSettings,
) : GameSession {
    val engine = TetrisEngine(seed)
    private val renderer = TetrisRenderer(seed)
    val palette = renderer.palette

    private val _state = MutableStateFlow<GameStatus>(GameStatus.Playing)
    override val state: StateFlow<GameStatus> = _state.asStateFlow()

    private val _uiState = MutableStateFlow(TetrisUiState(settings = settings))
    val uiState: StateFlow<TetrisUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TetrisEvent>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<TetrisEvent> = _events.asSharedFlow()

    private var paused = false

    init {
        engine.eventSink = ::onEngineEvent
        publish()
    }

    override fun update(dtMillis: Long) {
        if (paused) return
        if (_state.value is GameStatus.GameOver) {
            renderer.effects.update(dtMillis)
            return
        }
        engine.update(dtMillis)
        renderer.effects.update(dtMillis)
        publish()
    }

    override fun onInput(event: InputEvent) {
        if (event is InputEvent.TogglePause) {
            togglePause()
            return
        }
        if (paused || engine.gameOver) return

        when (event) {
            is InputEvent.HorizontalHold -> engine.onHorizontalHold(event.direction, event.pressed)
            InputEvent.RotateClockwise -> engine.rotate(1)
            InputEvent.RotateCounterClockwise -> engine.rotate(-1)
            InputEvent.HardDrop -> engine.hardDrop()
            is InputEvent.SoftDrop -> engine.setSoftDrop(event.pressed)
            InputEvent.HoldPiece -> engine.hold()
            InputEvent.TogglePause -> Unit
        }
        publish()
    }

    override fun render(canvas: DrawScope) {
        renderer.render(canvas, engine)
    }

    private fun togglePause() {
        if (engine.gameOver) return
        paused = !paused
        _state.value = if (paused) GameStatus.Paused else GameStatus.Playing
        publish()
    }

    private fun onEngineEvent(event: TetrisEvent) {
        _events.tryEmit(event)
        when (event) {
            is TetrisEvent.LineCleared -> renderer.effects.onLineClear(event.rows, renderer.palette)
            is TetrisEvent.LevelUp -> renderer.effects.onLevelUp()
            is TetrisEvent.GameOver -> {
                paused = false
                renderer.effects.triggerGameOver()
                _state.value = GameStatus.GameOver(event.score)
            }

            else -> Unit
        }
        publish()
    }

    private fun publish() {
        _uiState.value = TetrisUiState(
            status = _state.value,
            score = engine.tracker.score,
            lines = engine.tracker.lines,
            level = engine.tracker.level,
            combo = engine.tracker.combo,
            backToBack = engine.tracker.backToBack,
            next = engine.preview,
            hold = engine.holdPiece,
            canHold = engine.canHold,
            settings = settings,
        )
    }
}
