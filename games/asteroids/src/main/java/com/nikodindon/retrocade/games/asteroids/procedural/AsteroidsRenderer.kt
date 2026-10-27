package com.nikodindon.retrocade.games.asteroids.procedural

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.nikodindon.retrocade.core.common.Rng
import com.nikodindon.retrocade.games.asteroids.logic.Asteroid
import com.nikodindon.retrocade.games.asteroids.logic.AsteroidsEngine
import com.nikodindon.retrocade.games.asteroids.logic.BULLET_RADIUS
import com.nikodindon.retrocade.games.asteroids.logic.SAUCER_RADIUS
import com.nikodindon.retrocade.games.asteroids.logic.SHIP_RADIUS
import com.nikodindon.retrocade.games.asteroids.logic.WORLD_HEIGHT
import com.nikodindon.retrocade.games.asteroids.logic.WORLD_WIDTH
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Draws the logical square world, centred and scaled to the canvas.
 *
 * A single mutable [Path] is reused for every polygon and the stroke is cached
 * until the canvas size changes. Star positions are generated once in the
 * constructor from the game seed; only their twinkle is evaluated per frame.
 */
class AsteroidsRenderer(seed: Long) {
    val palette = AsteroidsPalette(seed)
    val effects = AsteroidsEffects(seed)
    private val stars = StarField(seed)
    private val path = Path()
    private var elapsedMs = 0L

    private var cachedStroke: Stroke? = null
    private var cachedStrokeWidth = -1f
    private var cachedFlameStroke: Stroke? = null
    private var cachedFlameStrokeWidth = -1f

    fun update(dtMillis: Long) {
        elapsedMs += dtMillis
        effects.update(dtMillis)
    }

    fun render(scope: DrawScope, engine: AsteroidsEngine) {
        if (scope.size.width <= 0f || scope.size.height <= 0f) return
        val scale = min(scope.size.width / WORLD_WIDTH, scope.size.height / WORLD_HEIGHT)
        if (scale <= 0f) return
        val originX = (scope.size.width - WORLD_WIDTH * scale) / 2f
        val originY = (scope.size.height - WORLD_HEIGHT * scale) / 2f

        scope.drawRect(color = palette.worldBackground)
        stars.draw(scope, originX, originY, scale, elapsedMs)

        val stroke = strokeFor(scale)
        drawAsteroids(scope, engine, originX, originY, scale, stroke)
        drawBullets(scope, engine, originX, originY, scale)
        drawSaucer(scope, engine, originX, originY, scale, stroke)
        drawShip(scope, engine, originX, originY, scale, stroke)
        effects.render(scope, originX, originY, scale)
    }

    private fun drawAsteroids(
        scope: DrawScope,
        engine: AsteroidsEngine,
        originX: Float,
        originY: Float,
        scale: Float,
        stroke: Stroke,
    ) {
        var index = 0
        while (index < engine.asteroids.size) {
            val asteroid = engine.asteroids[index]
            drawAsteroidAt(scope, asteroid, originX, originY, scale, stroke, 0f, 0f)

            val radius = asteroid.size.radius
            val wrappedX = when {
                asteroid.x < radius -> WORLD_WIDTH
                asteroid.x > WORLD_WIDTH - radius -> -WORLD_WIDTH
                else -> 0f
            }
            val wrappedY = when {
                asteroid.y < radius -> WORLD_HEIGHT
                asteroid.y > WORLD_HEIGHT - radius -> -WORLD_HEIGHT
                else -> 0f
            }
            if (wrappedX != 0f) {
                drawAsteroidAt(scope, asteroid, originX, originY, scale, stroke, wrappedX, 0f)
            }
            if (wrappedY != 0f) {
                drawAsteroidAt(scope, asteroid, originX, originY, scale, stroke, 0f, wrappedY)
            }
            if (wrappedX != 0f && wrappedY != 0f) {
                drawAsteroidAt(scope, asteroid, originX, originY, scale, stroke, wrappedX, wrappedY)
            }
            index++
        }
    }

    private fun drawAsteroidAt(
        scope: DrawScope,
        asteroid: Asteroid,
        originX: Float,
        originY: Float,
        scale: Float,
        stroke: Stroke,
        wrapX: Float,
        wrapY: Float,
    ) {
        val centerX = originX + (asteroid.x + wrapX) * scale
        val centerY = originY + (asteroid.y + wrapY) * scale
        val shape = asteroid.shape
        if (shape.isEmpty()) return

        path.rewind()
        var i = 0
        while (i < shape.size) {
            val angle = asteroid.angle + (PI * 2.0 * i / shape.size).toFloat()
            val distance = asteroid.size.radius * shape[i] * scale
            val px = centerX + cos(angle) * distance
            val py = centerY + sin(angle) * distance
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            i++
        }
        path.close()

        val color = palette.asteroid(asteroid.colorIndex)
        scope.drawPath(path = path, color = color.copy(alpha = 0.08f))
        scope.drawPath(path = path, color = color, style = stroke)

        val hairline = (stroke.width * 0.62f).coerceAtLeast(1f)
        val tip = stroke.width
        var chord = 0
        while (chord < 2) {
            val a = chord * 3
            val b = a + shape.size / 2
            val angleA = asteroid.angle + (PI * 2.0 * (a % shape.size) / shape.size).toFloat()
            val angleB = asteroid.angle + (PI * 2.0 * (b % shape.size) / shape.size).toFloat()
            val radiusA = asteroid.size.radius * shape[a % shape.size] * scale * 0.78f
            val radiusB = asteroid.size.radius * shape[b % shape.size] * scale * 0.78f
            scope.drawLine(
                color = color.copy(alpha = 0.34f),
                start = Offset(centerX + cos(angleA) * radiusA, centerY + sin(angleA) * radiusA),
                end = Offset(centerX + cos(angleB) * radiusB, centerY + sin(angleB) * radiusB),
                strokeWidth = hairline,
            )
            chord++
        }
        // Tiny bright notch gives each rock a hand-drawn vector look.
        scope.drawCircle(
            color = Color.White.copy(alpha = 0.32f),
            radius = tip,
            center = Offset(centerX, centerY),
        )
    }

    private fun drawBullets(
        scope: DrawScope,
        engine: AsteroidsEngine,
        originX: Float,
        originY: Float,
        scale: Float,
    ) {
        var index = 0
        while (index < engine.bullets.size) {
            val bullet = engine.bullets[index]
            val px = originX + bullet.x * scale
            val py = originY + bullet.y * scale
            val color = if (bullet.fromPlayer) palette.playerBullet else palette.enemyBullet
            scope.drawLine(
                color = color.copy(alpha = 0.36f),
                start = Offset(px - bullet.vx * 0.018f * scale, py - bullet.vy * 0.018f * scale),
                end = Offset(px, py),
                strokeWidth = (BULLET_RADIUS * scale).coerceAtLeast(1f),
            )
            scope.drawCircle(
                color = color,
                radius = (BULLET_RADIUS * scale).coerceAtLeast(1.5f),
                center = Offset(px, py),
            )
            index++
        }
    }

    private fun drawSaucer(
        scope: DrawScope,
        engine: AsteroidsEngine,
        originX: Float,
        originY: Float,
        scale: Float,
        stroke: Stroke,
    ) {
        val saucer = engine.saucer ?: return
        val centerX = originX + saucer.x * scale
        val centerY = originY + saucer.y * scale
        val radius = SAUCER_RADIUS * scale
        path.rewind()
        path.moveTo(centerX - radius, centerY)
        path.lineTo(centerX - radius * 0.45f, centerY - radius * 0.5f)
        path.lineTo(centerX + radius * 0.45f, centerY - radius * 0.5f)
        path.lineTo(centerX + radius, centerY)
        path.lineTo(centerX + radius * 0.55f, centerY + radius * 0.5f)
        path.lineTo(centerX - radius * 0.55f, centerY + radius * 0.5f)
        path.close()
        scope.drawPath(path = path, color = palette.saucer.copy(alpha = 0.14f))
        scope.drawPath(path = path, color = palette.saucer, style = stroke)
        scope.drawLine(
            color = palette.saucer.copy(alpha = 0.7f),
            start = Offset(centerX - radius * 0.7f, centerY),
            end = Offset(centerX + radius * 0.7f, centerY),
            strokeWidth = maxOf(1f, stroke.width * 0.7f),
        )
        scope.drawCircle(
            color = palette.saucer,
            radius = radius * 0.16f,
            center = Offset(centerX, centerY - radius * 0.12f),
        )
    }

    private fun drawShip(
        scope: DrawScope,
        engine: AsteroidsEngine,
        originX: Float,
        originY: Float,
        scale: Float,
        stroke: Stroke,
    ) {
        val ship = engine.ship
        if (!ship.alive) return

        val blink = ship.invulnerabilityMs > 0f && (elapsedMs / 110L) % 2L == 0L
        val alpha = if (blink) 0.35f else 1f
        val centerX = originX + ship.x * scale
        val centerY = originY + ship.y * scale
        val radius = SHIP_RADIUS * scale * 1.55f
        val angle = ship.angle.toDouble()

        if (engine.isThrusting && !blink) {
            val flicker = 0.65f + 0.35f * sin(elapsedMs * 0.045f)
            val flame = radius * (1.1f + flicker * 0.45f)
            path.rewind()
            path.moveTo(
                centerX + cos(angle + 2.55).toFloat() * radius * 0.45f,
                centerY + sin(angle + 2.55).toFloat() * radius * 0.45f,
            )
            path.lineTo(
                centerX - cos(angle).toFloat() * flame,
                centerY - sin(angle).toFloat() * flame,
            )
            path.lineTo(
                centerX + cos(angle - 2.55).toFloat() * radius * 0.45f,
                centerY + sin(angle - 2.55).toFloat() * radius * 0.45f,
            )
            path.close()
            scope.drawPath(path = path, color = palette.ship.copy(alpha = 0.45f * alpha))
            scope.drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.7f * alpha),
                style = flameStrokeFor(scale),
            )
        }

        path.rewind()
        path.moveTo(
            centerX + cos(angle).toFloat() * radius,
            centerY + sin(angle).toFloat() * radius,
        )
        path.lineTo(
            centerX + cos(angle + 2.45).toFloat() * radius * 0.75f,
            centerY + sin(angle + 2.45).toFloat() * radius * 0.75f,
        )
        path.lineTo(
            centerX + cos(angle + PI).toFloat() * radius * 0.38f,
            centerY + sin(angle + PI).toFloat() * radius * 0.38f,
        )
        path.lineTo(
            centerX + cos(angle - 2.45).toFloat() * radius * 0.75f,
            centerY + sin(angle - 2.45).toFloat() * radius * 0.75f,
        )
        path.close()
        scope.drawPath(path = path, color = palette.ship.copy(alpha = 0.15f * alpha))
        scope.drawPath(path = path, color = palette.ship.copy(alpha = alpha), style = stroke)
    }

    private fun flameStrokeFor(scale: Float): Stroke {
        val width = maxOf(1f, strokeFor(scale).width * 0.6f)
        val cached = cachedFlameStroke
        if (cached == null || cachedFlameStrokeWidth != width) {
            val created = Stroke(width = width)
            cachedFlameStroke = created
            cachedFlameStrokeWidth = width
            return created
        }
        return cached
    }

    private fun strokeFor(scale: Float): Stroke {
        val width = (2.1f * scale).coerceAtLeast(1.4f)
        val cached = cachedStroke
        if (cached == null || cachedStrokeWidth != width) {
            val created = Stroke(width = width)
            cachedStroke = created
            cachedStrokeWidth = width
            return created
        }
        return cached
    }

    /** Star data is seeded once and then only animated, never regenerated. */
    private class StarField(seed: Long) {
        private val rng = Rng(seed xor 0x57A45EEDL)
        private val count = 112
        private val x = FloatArray(count)
        private val y = FloatArray(count)
        private val size = FloatArray(count)
        private val phase = FloatArray(count)
        private val brightness = FloatArray(count)

        init {
            var i = 0
            while (i < count) {
                x[i] = rng.nextFloat(0f, WORLD_WIDTH)
                y[i] = rng.nextFloat(0f, WORLD_HEIGHT)
                size[i] = rng.nextFloat(0.55f, 1.8f)
                phase[i] = rng.nextFloat(0.4f, 2.8f)
                brightness[i] = rng.nextFloat(0.18f, 0.7f)
                i++
            }
        }

        fun draw(
            scope: DrawScope,
            originX: Float,
            originY: Float,
            scale: Float,
            elapsedMs: Long,
        ) {
            var i = 0
            val t = elapsedMs * 0.001f
            while (i < count) {
                val twinkle = 0.5f + 0.5f * sin(t * phase[i] + phase[i])
                val alpha = (brightness[i] * (0.55f + twinkle * 0.45f)).coerceIn(0.05f, 0.9f)
                scope.drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = (size[i] * scale * 0.75f).coerceAtLeast(0.6f),
                    center = Offset(originX + x[i] * scale, originY + y[i] * scale),
                )
                i++
            }
        }
    }
}
