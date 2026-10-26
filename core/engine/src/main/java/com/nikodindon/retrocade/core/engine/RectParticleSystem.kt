package com.nikodindon.retrocade.core.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.max

/**
 * Pooled rectangular particles. No allocation happens while updating or drawing:
 * arrays are pre-allocated and the pool is reused across line clears.
 */
class RectParticleSystem(private val capacity: Int = 512) {
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
            // Pool is full: overwrite the next slot, which is the oldest by rotation.
            next.also { next = (next + 1) % capacity }
        }
        this.x[index] = x
        this.y[index] = y
        this.vx[index] = vx
        this.vy[index] = vy
        life[index] = lifeMillis
        maxLife[index] = max(1f, lifeMillis)
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
                vy[i] += GRAVITY * dtSeconds
                vx[i] *= DRAG
                i++
            }
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

    fun draw(scope: DrawScope, originX: Float, originY: Float, scale: Float) {
        var i = 0
        while (i < active) {
            val alpha = (life[i] / maxLife[i]).coerceIn(0f, 1f)
            val side = size[i] * scale
            scope.drawRect(
                color = Color(colors[i]).copy(alpha = alpha),
                topLeft = Offset(originX + x[i] * scale, originY + y[i] * scale),
                size = Size(side, side),
            )
            i++
        }
    }

    private companion object {
        const val GRAVITY = 28f
        const val DRAG = 0.995f
    }
}
