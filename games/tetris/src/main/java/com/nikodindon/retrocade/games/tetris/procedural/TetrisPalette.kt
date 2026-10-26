package com.nikodindon.retrocade.games.tetris.procedural

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.nikodindon.retrocade.core.common.Rng

/**
 * Piece colours keep their classic identity (I cyan, O yellow, T purple, ...),
 * while hue shift, saturation and lightness are derived from the game seed.
 */
class TetrisPalette(seed: Long) {
    val seedLabel: String = "%08X".format(seed and 0xFFFFFFFFL)
    private val rng = Rng(seed xor 0x7E7C15L)
    private val hueShift = rng.nextFloat(-11f, 11f)
    private val saturation = rng.nextFloat(0.72f, 0.9f)
    private val lightness = rng.nextFloat(0.55f, 0.65f)

    private val anchors = floatArrayOf(
        0f,
        186f, // I cyan
        48f, // O yellow
        282f, // T purple
        138f, // S green
        354f, // Z red
        219f, // J blue
        26f, // L orange
    )

    val accent: Color = Color.hsl((172f + hueShift) % 360f, 0.9f, 0.62f)
    val boardBackground: Color = Color.hsl((222f + hueShift * 0.3f) % 360f, 0.35f, 0.055f)
    val grid: Color = Color.hsl((212f + hueShift * 0.3f) % 360f, 0.42f, 0.18f)
    val border: Color = Color.hsl((172f + hueShift) % 360f, 0.5f, 0.32f)

    private val colors: Array<Color> = Array(anchors.size) { index ->
        if (index == 0) {
            Color.Transparent
        } else {
            Color.hsl(
                ((anchors[index] + hueShift + 360f) % 360f),
                saturation,
                lightness,
            )
        }
    }

    fun piece(colorIndex: Int): Color =
        colors.getOrElse(colorIndex) { Color.Magenta }

    fun pale(colorIndex: Int, alpha: Float): Color = piece(colorIndex).copy(alpha = alpha)

    fun particleArgb(index: Int): Int = piece(index).toArgb()
}
