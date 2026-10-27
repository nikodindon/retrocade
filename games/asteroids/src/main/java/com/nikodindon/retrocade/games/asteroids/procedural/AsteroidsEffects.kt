package com.nikodindon.retrocade.games.asteroids.procedural

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.nikodindon.retrocade.games.asteroids.logic.AsteroidSize
import com.nikodindon.retrocade.games.asteroids.logic.AsteroidsEvent
import com.nikodindon.retrocade.core.common.Rng
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * Explosions and short flashes. Particles are pooled; the only allocations here
 * happen on resize or when the effect object is created.
 */
class AsteroidsEffects(seed: Long) {
    private val rng = Rng(seed xor 0x0EFFECC7L)
    private val particles = AsteroidsParticleSystem()
    private var flashMs = 0f

    fun update(dtMillis: Long) {
        particles.update(dtMillis)
        flashMs = max(0f, flashMs - dtMillis)
    }

    fun onAsteroidDestroyed(event: AsteroidsEvent.AsteroidDestroyed, palette: AsteroidsPalette) {
        val count = when (event.size) {
            AsteroidSize.LARGE -> 24
            AsteroidSize.MEDIUM -> 16
            AsteroidSize.SMALL -> 10
        }
        val radius = when (event.size) {
            AsteroidSize.LARGE -> 140f
            AsteroidSize.MEDIUM -> 95f
            AsteroidSize.SMALL -> 65f
        }
        explode(
            x = event.x,
            y = event.y,
            count = count,
            speed = radius,
            colorArgb = palette.explosionArgb(event.colorIndex),
        )
    }

    fun onSaucerDestroyed(x: Float, y: Float, palette: AsteroidsPalette) {
        explode(x, y, 30, 180f, palette.saucerArgb)
    }

    fun onShipHit(x: Float, y: Float, palette: AsteroidsPalette) {
        explode(x, y, 34, 190f, palette.shipArgb)
        flashMs = 220f
    }

    fun onGameOver() {
        flashMs = 360f
    }

    fun render(scope: DrawScope, originX: Float, originY: Float, scale: Float) {
        particles.draw(scope, originX, originY, scale)
        if (flashMs > 0f) {
            scope.drawRect(
                color = Color.White.copy(alpha = (flashMs / 360f) * 0.12f),
                topLeft = Offset.Zero,
                size = Size(scope.size.width, scope.size.height),
            )
        }
    }

    private fun explode(
        x: Float,
        y: Float,
        count: Int,
        speed: Float,
        colorArgb: Int,
    ) {
        repeat(count) {
            val angle = rng.nextFloat(0f, (PI * 2.0).toFloat())
            val velocity = speed * rng.nextFloat(0.35f, 1.15f)
            particles.spawn(
                x = x,
                y = y,
                vx = cos(angle) * velocity,
                vy = sin(angle) * velocity,
                lifeMillis = rng.nextFloat(280f, 720f),
                size = rng.nextFloat(1.4f, 3.6f),
                colorArgb = colorArgb,
            )
        }
    }
}
