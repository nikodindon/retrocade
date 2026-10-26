package com.nikodindon.retrocade.core.engine

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import com.nikodindon.retrocade.games.api.GameSession
import kotlinx.coroutines.isActive

/**
 * Runs [session] at a fixed 60 updates per second, driven by the Compose frame
 * clock. Returns a frame counter that the renderer observes to request a redraw.
 *
 * The simulation is paused by setting [running] to false; accumulated time is
 * discarded so unpausing never fast-forwards the game.
 */
@Composable
fun rememberGameFrame(
    session: GameSession,
    running: Boolean,
    updatesPerSecond: Int = 60,
): State<Long> {
    val stepMillis = (1_000.0 / updatesPerSecond).toLong().coerceAtLeast(1L)
    val frame = remember(session) { mutableLongStateOf(0L) }

    LaunchedEffect(session, running, stepMillis) {
        if (!running) return@LaunchedEffect

        val clock = FixedStepClock(stepMillis)
        var lastNanos = withFrameNanos { it }

        while (isActive) {
            val nowNanos = withFrameNanos { it }
            val deltaMillis = (nowNanos - lastNanos) / 1_000_000L
            lastNanos = nowNanos

            repeat(clock.advance(deltaMillis)) {
                session.update(stepMillis)
            }
            frame.longValue++
        }
    }

    return frame
}
