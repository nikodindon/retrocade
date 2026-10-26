package com.nikodindon.retrocade.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.nikodindon.retrocade.core.common.Rng

/**
 * Procedural CRT-ish backdrop: a soft vignette, a sparse pixel starfield and
 * horizontal scanlines. Drawn with plain rectangles, no assets and no per-frame
 * state mutation.
 */
@Composable
fun RetroBackdrop(
    modifier: Modifier = Modifier,
    seed: Long = 0x5EED_CAFE,
    content: @Composable () -> Unit = {},
) {
    val stars = remember(seed) {
        val rng = Rng(seed)
        List(70) {
            Triple(rng.nextFloat(), rng.nextFloat(), rng.nextFloat())
        }
    }

    Box(modifier = modifier.background(RetroColors.Background)) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF16233B), RetroColors.Background),
                    center = Offset(size.width * 0.5f, size.height * 0.34f),
                    radius = size.maxDimension * 0.9f,
                ),
            )

            stars.forEach { (x, y, brightness) ->
                val side = if (brightness > 0.86f) 3f else 2f
                drawRect(
                    color = when {
                        brightness > 0.92f -> RetroColors.NeonCyan.copy(alpha = 0.45f)
                        brightness > 0.7f -> RetroColors.TextMuted.copy(alpha = 0.35f)
                        else -> RetroColors.Grid.copy(alpha = 0.5f)
                    },
                    topLeft = Offset(x * size.width, y * size.height),
                    size = androidx.compose.ui.geometry.Size(side, side),
                )
            }

            val lineColor = RetroColors.Black.copy(alpha = 0.14f)
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = lineColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f,
                )
                y += 4f
            }
        }
        content()
    }
}

/** Lightweight overlay for game screens; must stay non-interactive. */
@Composable
fun ScanlineOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val yStep = 5f
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = RetroColors.Black.copy(alpha = 0.08f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f,
            )
            y += yStep
        }
    }
}
