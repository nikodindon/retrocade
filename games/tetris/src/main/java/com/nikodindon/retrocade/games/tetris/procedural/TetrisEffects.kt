package com.nikodindon.retrocade.games.tetris.procedural

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.nikodindon.retrocade.core.common.Rng
import com.nikodindon.retrocade.core.engine.RectParticleSystem
import com.nikodindon.retrocade.games.tetris.logic.HIDDEN_ROWS

/**
 * Pure canvas effects for line clears, level ups and game over. Particles live in
 * board-cell coordinates and are rendered with a cached particle pool.
 */
class TetrisEffects(seed: Long) {
    private val rng = Rng(seed xor 0x51A7E)
    private val particles = RectParticleSystem(capacity = 384)
    private var flashRemainingMs = 0f
    private var glitchRemainingMs = 0f

    val glitching: Boolean get() = glitchRemainingMs > 0f

    fun onLineClear(rows: List<Int>, palette: TetrisPalette) {
        flashRemainingMs = 240f
        rows.forEach { row ->
            val visibleRow = (row - HIDDEN_ROWS).toFloat()
            repeat(18) {
                particles.spawn(
                    x = rng.nextFloat(0f, 10f),
                    y = visibleRow + rng.nextFloat(0f, 0.9f),
                    vx = rng.nextFloat(-5f, 5f),
                    vy = rng.nextFloat(-9f, -2f),
                    lifeMillis = rng.nextFloat(260f, 720f),
                    size = rng.nextFloat(0.12f, 0.34f),
                    colorArgb = palette.particleArgb(rng.nextInt(1, 8)),
                )
            }
        }
    }

    fun onLevelUp() {
        flashRemainingMs = 180f
    }

    fun triggerGameOver() {
        glitchRemainingMs = 1_200f
    }

    fun update(dtMillis: Long) {
        if (dtMillis <= 0) return
        particles.update(dtMillis)
        flashRemainingMs = (flashRemainingMs - dtMillis).coerceAtLeast(0f)
        glitchRemainingMs = (glitchRemainingMs - dtMillis).coerceAtLeast(0f)
    }

    fun render(scope: DrawScope, originX: Float, originY: Float, cell: Float) {
        particles.draw(scope, originX, originY, cell)
        if (flashRemainingMs > 0f) {
            scope.drawRect(
                color = Color.White.copy(alpha = 0.18f * (flashRemainingMs / 240f)),
                topLeft = Offset(originX, originY),
                size = Size(cell * 10f, cell * 20f),
            )
        }
    }

    fun clear() {
        particles.clear()
        flashRemainingMs = 0f
        glitchRemainingMs = 0f
    }
}
