package com.nikodindon.retrocade.games.asteroids.procedural

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.nikodindon.retrocade.core.common.Rng
import com.nikodindon.retrocade.core.ui.RetroColors

/**
 * Space palette derived from the run seed. Asteroids still keep a small,
 * readable set of colours; the seed shifts the whole range instead of producing
 * an arbitrary colour per rock.
 */
class AsteroidsPalette(seed: Long) {
    private val rng = Rng(seed xor 0x0A57E20D5L)
    private val hueShift = rng.nextFloat(-24f, 24f)
    private val saturation = rng.nextFloat(0.62f, 0.9f)
    private val lightness = rng.nextFloat(0.56f, 0.68f)

    private val asteroidHues = floatArrayOf(174f, 202f, 286f, 36f)

    val worldBackground: Color = RetroColors.Background
    val ship: Color = Color.hsl(hue(48f + hueShift * 0.25f), 0.82f, 0.68f)
    val saucer: Color = Color.hsl(hue(330f + hueShift * 0.35f), 0.78f, 0.65f)
    val playerBullet: Color = Color.hsl(hue(158f + hueShift), 0.94f, 0.72f)
    val enemyBullet: Color = Color.hsl(hue(4f + hueShift), 0.9f, 0.62f)

    private val asteroidColors: Array<Color> = Array(asteroidHues.size) { index ->
        Color.hsl(
            hue(asteroidHues[index] + hueShift),
            saturation,
            lightness,
        )
    }

    fun asteroid(colorIndex: Int): Color =
        asteroidColors[colorIndex.mod(asteroidColors.size)]

    fun asteroidArgb(colorIndex: Int): Int = asteroid(colorIndex).toArgb()

    fun explosionArgb(colorIndex: Int): Int = asteroid(colorIndex).toArgb()

    val shipArgb: Int = ship.toArgb()
    val saucerArgb: Int = saucer.toArgb()

    private fun hue(value: Float): Float {
        val normalized = value % 360f
        return if (normalized < 0f) normalized + 360f else normalized
    }
}
