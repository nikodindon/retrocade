package com.nikodindon.retrocade.games.tetris.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.nikodindon.retrocade.games.api.InputEvent
import kotlin.math.abs

/**
 * Touch mapping:
 * - left/right third: move on press, hold to repeat (DAS/ARR in the engine)
 * - centre tap: clockwise rotation
 * - two-finger touch: counter-clockwise rotation
 * - vertical drag: soft drop; upward drag: hard drop
 *
 * [thresholdPx] comes from the user's swipe sensitivity setting.
 */
fun Modifier.tetrisTouchInput(
    thresholdPx: Float,
    onInput: (InputEvent) -> Unit,
): Modifier = pointerInput(thresholdPx) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val start = down.position
        val direction = when {
            start.x < size.width * 0.34f -> -1
            start.x > size.width * 0.66f -> 1
            else -> 0
        }

        var current = start
        var held = false
        var twoFingers = false
        var softDrop = false
        var hardDropped = false
        var dragDistance = 0f

        if (direction != 0) {
            held = true
            onInput(InputEvent.HorizontalHold(direction, true))
        }

        var anyPressed = true
        while (anyPressed) {
            val event = awaitPointerEvent()
            anyPressed = event.changes.any { it.pressed }
            val pressedCount = event.changes.count { it.pressed }
            if (pressedCount >= 2 && !twoFingers) {
                twoFingers = true
                if (held) {
                    held = false
                    onInput(InputEvent.HorizontalHold(direction, false))
                }
                if (softDrop) {
                    softDrop = false
                    onInput(InputEvent.SoftDrop(false))
                }
                onInput(InputEvent.RotateCounterClockwise)
            }

            val change = event.changes.firstOrNull { it.id == down.id }
            if (change != null && change.pressed) {
                current = change.position
                val dx = current.x - start.x
                val dy = current.y - start.y
                dragDistance = maxOf(dragDistance, abs(dx) + abs(dy))

                if (dy > thresholdPx && !softDrop) {
                    softDrop = true
                    onInput(InputEvent.SoftDrop(true))
                }
                if (dy < -thresholdPx && !hardDropped) {
                    hardDropped = true
                    onInput(InputEvent.HardDrop)
                }
                change.consume()
            }
        }

        if (held) {
            onInput(InputEvent.HorizontalHold(direction, false))
        }
        if (softDrop) {
            onInput(InputEvent.SoftDrop(false))
        }
        if (!twoFingers && !softDrop && !hardDropped && dragDistance < thresholdPx && direction == 0) {
            onInput(InputEvent.RotateClockwise)
        }
    }
}
