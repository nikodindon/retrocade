package com.nikodindon.retrocade.games.asteroids.procedural

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Pooled, allocation-free particle variant for space explosions.
 *
 * Unlike [com.nikodindon.retrocade.core.engine.RectParticleSystem] there is no
 * gravity: debris drifts with a light drag and is drawn as a short velocity
 * streak. Arrays are pre-allocated and entities are swapped on expiry.
 */
class AsteroidsParticleSystem(private val capacity: Int = 640) {
    private val x = FloatArray(capacity)
    private val y = FloatArray(capacity)
    private val vx = FloatArray(capacity)
    private val vy = FloatArray(capacity)
    private val life = FloatArray(capacity)
    private val maxLife = FloatArray(capacity)
    private val size = FloatArray(capacity)
    private val colors = IntArray(capacity)

    private var active = 0
    private var next = 0

    val activeCount: Int get() = active

    fun clear() {
        active = 0
        next = 0
    }

    fun spawn(
        x: Float,
        y: Float,
        vx: Float,
        vy: Float,
        lifeMillis: Float,
        size: Float,
        colorArgb: Int,
    ) {
        val index = if (active < capacity) {
            active++
            next.also { next = (next + 1) % capacity }
        } else {
            next.also { next = (next + 1) % capacity }
        }
        this.x[index] = x
        this.y[index] = y
        this.vx[index] = vx
        this.vy[index] = vy
        life[index] = lifeMillis
        maxLife[index] = maxOf(1f, lifeMillis)
        this.size[index] = size
        colors[index] = colorArgb
    }

    fun update(dtMillis: Long) {
        val dtSeconds = dtMillis / 1_000f
        var i = 0
        while (i < active) {
            life[i] -= dtMillis
            if (life[i] <= 0f) {
                removeAt(i)
            } else {
                x[i] += vx[i] * dtSeconds
                y[i] += vy[i] * dtSeconds
                vx[i] *= 0.992f
                vy[i] *= 0.992f
                i++
            }
        }
    }

    fun draw(scope: DrawScope, originX: Float, originY: Float, scale: Float) {
        var i = 0
        while (i < active) {
            val alpha = (life[i] / maxLife[i]).coerceIn(0f, 1f)
            val px = originX + x[i] * scale
            val py = originY + y[i] * scale
            scope.drawLine(
                color = Color(colors[i]).copy(alpha = alpha),
                start = Offset(px, py),
                end = Offset(px - vx[i] * 0.022f * scale, py - vy[i] * 0.022f * scale),
                strokeWidth = (size[i] * scale).coerceAtLeast(1f),
            )
            i++
        }
    }

    private fun removeAt(index: Int) {
        val last = active - 1
        if (index != last) {
            x[index] = x[last]
            y[index] = y[last]
            vx[index] = vx[last]
            vy[index] = vy[last]
            life[index] = life[last]
            maxLife[index] = maxLife[last]
            size[index] = size[last]
            colors[index] = colors[last]
        }
        active = last
    }
}
