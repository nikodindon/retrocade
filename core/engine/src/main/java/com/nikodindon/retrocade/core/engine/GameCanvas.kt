package com.nikodindon.retrocade.core.engine

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import com.nikodindon.retrocade.games.api.GameSession

/**
 * Canvas host for a [GameSession]. Reading [frame] inside the draw block makes
 * Compose redraw without recomposing the whole screen on every game tick.
 */
@Composable
fun GameCanvas(
    session: GameSession,
    frame: State<Long>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        // Deliberately read the counter in the draw phase: it only invalidates draw.
        @Suppress("UNUSED_EXPRESSION")
        frame.value
        session.render(this)
    }
}
