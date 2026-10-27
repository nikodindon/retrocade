package com.nikodindon.retrocade.games.asteroids.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.nikodindon.retrocade.games.api.InputEvent

private const val ZONE_LEFT = 0
private const val ZONE_RIGHT = 1
private const val ZONE_THRUST = 2
private const val ZONE_FIRE = 3

/**
 * Full-canvas multi-touch zones:
 *
 * - top quarter: fire (tap)
 * - bottom quarter: thrust (hold)
 * - middle-left / middle-right: rotate (hold)
 *
 * Pointer ids are tracked independently so turning, thrusting and firing can
 * overlap. The zones are transparent and never draw over the ship.
 */
fun Modifier.asteroidsTouchInput(onInput: (InputEvent) -> Unit): Modifier = pointerInput(Unit) {
    val pointerZones = mutableMapOf<Long, Int>()

    fun zoneAt(x: Float, y: Float, width: Float, height: Float): Int = when {
        y < height * 0.28f -> ZONE_FIRE
        y > height * 0.72f -> ZONE_THRUST
        x < width * 0.5f -> ZONE_LEFT
        else -> ZONE_RIGHT
    }

    fun press(zone: Int) {
        when (zone) {
            ZONE_LEFT -> onInput(InputEvent.HorizontalHold(-1, true))
            ZONE_RIGHT -> onInput(InputEvent.HorizontalHold(1, true))
            ZONE_THRUST -> onInput(InputEvent.SoftDrop(true))
            ZONE_FIRE -> onInput(InputEvent.HardDrop)
        }
    }

    fun release(zone: Int) {
        if (pointerZones.values.any { it == zone }) return
        when (zone) {
            ZONE_LEFT -> onInput(InputEvent.HorizontalHold(-1, false))
            ZONE_RIGHT -> onInput(InputEvent.HorizontalHold(1, false))
            ZONE_THRUST -> onInput(InputEvent.SoftDrop(false))
            ZONE_FIRE -> Unit
        }
    }

    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent()
            var changeIndex = 0
            while (changeIndex < event.changes.size) {
                val change = event.changes[changeIndex]
                val id = change.id.value
                if (change.pressed && !change.previousPressed) {
                    val zone = zoneAt(
                        x = change.position.x,
                        y = change.position.y,
                        width = size.width.toFloat(),
                        height = size.height.toFloat(),
                    )
                    pointerZones[id] = zone
                    press(zone)
                } else if (!change.pressed && change.previousPressed) {
                    val zone = pointerZones.remove(id)
                    if (zone != null) release(zone)
                }
                change.consume()
                changeIndex++
            }
        }
    }
}
