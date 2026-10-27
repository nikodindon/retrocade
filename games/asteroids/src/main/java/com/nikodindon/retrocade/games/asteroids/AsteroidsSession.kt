package com.nikodindon.retrocade.games.asteroids

import androidx.compose.ui.graphics.drawscope.DrawScope
import com.nikodindon.retrocade.games.api.GameSession
import com.nikodindon.retrocade.games.api.GameSettings
import com.nikodindon.retrocade.games.api.GameStatus
import com.nikodindon.retrocade.games.api.InputEvent
import com.nikodindon.retrocade.games.asteroids.logic.AsteroidsEngine
import com.nikodindon.retrocade.games.asteroids.logic.AsteroidsEvent
import com.nikodindon.retrocade.games.asteroids.procedural.AsteroidsRenderer
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class AsteroidsSession(
    val seed: Long,
    val settings: GameSettings,
    startingWave: Int = 1,
) : GameSession {
    val engine = AsteroidsEngine(seed, startingWave)
    val renderer = AsteroidsRenderer(seed)
    val palette = renderer.palette

    private val _state = MutableStateFlow<GameStatus>(GameStatus.Playing)
    override val state: StateFlow<GameStatus> = _state.asStateFlow()

    private val _uiState = MutableStateFlow(AsteroidsUiState(settings = settings))
    val uiState: StateFlow<AsteroidsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AsteroidsEvent>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<AsteroidsEvent> = _events.asSharedFlow()

    private var paused = false
    private var leftHeld = false
    private var rightHeld = false

    init {
        engine.eventSink = ::onEngineEvent
        publish()
    }

    override fun update(dtMillis: Long) {
        if (paused) return
        if (_state.value is GameStatus.GameOver) {
            renderer.update(dtMillis)
            return
        }
        engine.update(dtMillis)
        renderer.update(dtMillis)
        publish()
    }

    override fun onInput(event: InputEvent) {
        if (event is InputEvent.TogglePause) {
            togglePause()
            return
        }
        if (paused || engine.gameOver) return

        when (event) {
            is InputEvent.HorizontalHold -> {
                when {
                    event.direction < 0 -> leftHeld = event.pressed
                    event.direction > 0 -> rightHeld = event.pressed
                }
                engine.setTurn(
                    when {
                        leftHeld && !rightHeld -> -1
                        rightHeld && !leftHeld -> 1
                        else -> 0
                    },
                )
            }

            is InputEvent.SoftDrop -> engine.setThrust(event.pressed)
            InputEvent.HardDrop -> engine.fire()
            InputEvent.RotateClockwise,
            InputEvent.RotateCounterClockwise,
            InputEvent.HoldPiece,
            -> Unit
            InputEvent.TogglePause -> Unit
        }
        publish()
    }

    override fun render(canvas: DrawScope) {
        renderer.render(canvas, engine)
    }

    fun togglePause() {
        if (engine.gameOver) return
        paused = !paused
        if (paused) {
            leftHeld = false
            rightHeld = false
            engine.setTurn(0)
            engine.setThrust(false)
        }
        _state.value = if (paused) GameStatus.Paused else GameStatus.Playing
        publish()
    }

    private fun onEngineEvent(event: AsteroidsEvent) {
        _events.tryEmit(event)
        when (event) {
            is AsteroidsEvent.AsteroidDestroyed -> {
                renderer.effects.onAsteroidDestroyed(event, palette)
            }

            is AsteroidsEvent.SaucerDestroyed -> {
                renderer.effects.onSaucerDestroyed(event.x, event.y, palette)
            }

            is AsteroidsEvent.ShipHit -> {
                renderer.effects.onShipHit(event.x, event.y, palette)
            }

            is AsteroidsEvent.GameOver -> {
                paused = false
                renderer.effects.onGameOver()
                _state.value = GameStatus.GameOver(event.score)
            }

            AsteroidsEvent.ExtraLife,
            AsteroidsEvent.Shot,
            AsteroidsEvent.ThrustStarted,
            AsteroidsEvent.SaucerAppeared,
            is AsteroidsEvent.WaveStarted,
            -> Unit
        }
        publish()
    }

    private fun publish() {
        _uiState.value = AsteroidsUiState(
            status = _state.value,
            score = engine.score,
            lives = engine.lives,
            wave = engine.wave,
            settings = settings,
        )
    }
}
