package com.nikodindon.retrocade.games.tetris.procedural

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import com.nikodindon.retrocade.games.tetris.logic.BOARD_HEIGHT
import com.nikodindon.retrocade.games.tetris.logic.BOARD_WIDTH
import com.nikodindon.retrocade.games.tetris.logic.HIDDEN_ROWS
import com.nikodindon.retrocade.games.tetris.logic.TetrisEngine
import com.nikodindon.retrocade.games.tetris.logic.VISIBLE_HEIGHT
import kotlin.math.min

/**
 * Canvas renderer. The static playfield background is generated once into an
 * ImageBitmap and reused; per-frame rendering only emits rectangles and lines.
 */
class TetrisRenderer(seed: Long) {
    val palette = TetrisPalette(seed)
    val effects = TetrisEffects(seed)

    private var background: ImageBitmap? = null
    private var backgroundWidth = 0
    private var backgroundHeight = 0

    fun render(scope: DrawScope, engine: TetrisEngine) {
        if (scope.size.width <= 0f || scope.size.height <= 0f) return
        val margin = 2f
        val cell = min(
            (scope.size.width - margin * 2f) / BOARD_WIDTH,
            (scope.size.height - margin * 2f) / VISIBLE_HEIGHT,
        )
        if (cell <= 0f) return

        val boardWidth = cell * BOARD_WIDTH
        val boardHeight = cell * VISIBLE_HEIGHT
        val originX = (scope.size.width - boardWidth) / 2f
        val originY = (scope.size.height - boardHeight) / 2f

        val bg = obtainBackground(boardWidth.toInt(), boardHeight.toInt())
        scope.drawImage(bg, topLeft = Offset(originX, originY))

        for (y in HIDDEN_ROWS until BOARD_HEIGHT) {
            val visibleY = y - HIDDEN_ROWS
            for (x in 0 until BOARD_WIDTH) {
                val colorIndex = engine.board.get(x, y)
                if (colorIndex != 0) {
                    drawBlock(
                        scope = scope,
                        x = originX + x * cell,
                        y = originY + visibleY * cell,
                        cell = cell,
                        color = palette.piece(colorIndex),
                        alpha = 1f,
                    )
                }
            }
        }

        val piece = engine.current
        if (piece != null) {
            var drop = 0
            while (!engine.board.collides(piece.cells, piece.x, piece.y + drop + 1)) {
                drop += 1
            }
            if (drop > 0) {
                val ghostColor = palette.piece(piece.type.colorIndex)
                piece.cells.forEach { cellOffset ->
                    val boardY = piece.y + cellOffset.y + drop
                    if (boardY >= HIDDEN_ROWS) {
                        drawGhost(
                            scope = scope,
                            x = originX + (piece.x + cellOffset.x) * cell,
                            y = originY + (boardY - HIDDEN_ROWS) * cell,
                            cell = cell,
                            color = ghostColor,
                        )
                    }
                }
            }

            piece.cells.forEach { cellOffset ->
                val boardY = piece.y + cellOffset.y
                if (boardY >= HIDDEN_ROWS) {
                    drawBlock(
                        scope = scope,
                        x = originX + (piece.x + cellOffset.x) * cell,
                        y = originY + (boardY - HIDDEN_ROWS) * cell,
                        cell = cell,
                        color = palette.piece(piece.type.colorIndex),
                        alpha = 1f,
                    )
                }
            }
        }

        effects.render(scope, originX, originY, cell)
    }

    private fun obtainBackground(width: Int, height: Int): ImageBitmap {
        val safeWidth = width.coerceAtLeast(1)
        val safeHeight = height.coerceAtLeast(1)
        val cached = background
        if (cached != null && backgroundWidth == safeWidth && backgroundHeight == safeHeight) {
            return cached
        }
        val image = buildBackground(safeWidth, safeHeight)
        background = image
        backgroundWidth = safeWidth
        backgroundHeight = safeHeight
        return image
    }

    private fun buildBackground(width: Int, height: Int): ImageBitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = Paint().apply { isAntiAlias = false }

        canvas.drawColor(palette.boardBackground.toArgb())

        paint.color = palette.grid.copy(alpha = 0.55f).toArgb()
        paint.strokeWidth = 1f
        for (x in 1 until BOARD_WIDTH) {
            val px = width.toFloat() / BOARD_WIDTH * x
            canvas.drawLine(px, 0f, px, height.toFloat(), paint)
        }
        for (y in 1 until VISIBLE_HEIGHT) {
            val py = height.toFloat() / VISIBLE_HEIGHT * y
            canvas.drawLine(0f, py, width.toFloat(), py, paint)
        }

        paint.color = Color.Black.copy(alpha = 0.16f).toArgb()
        var scanline = 0f
        while (scanline < height) {
            canvas.drawLine(0f, scanline, width.toFloat(), scanline, paint)
            scanline += 3f
        }

        paint.shader = RadialGradient(
            width / 2f,
            height / 2f,
            maxOf(width, height) * 0.72f,
            intArrayOf(Color.Transparent.toArgb(), Color.Black.copy(alpha = 0.5f).toArgb()),
            floatArrayOf(0.35f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = maxOf(2f, width / 260f)
        paint.color = palette.border.copy(alpha = 0.7f).toArgb()
        canvas.drawRect(
            paint.strokeWidth / 2f,
            paint.strokeWidth / 2f,
            width - paint.strokeWidth / 2f,
            height - paint.strokeWidth / 2f,
            paint,
        )

        return bitmap.asImageBitmap()
    }

    private fun drawBlock(
        scope: DrawScope,
        x: Float,
        y: Float,
        cell: Float,
        color: Color,
        alpha: Float,
    ) {
        val inset = cell * 0.045f
        val side = cell - inset * 2f
        val topLeft = Offset(x + inset, y + inset)
        scope.drawRect(
            color = color.copy(alpha = alpha),
            topLeft = topLeft,
            size = Size(side, side),
        )

        val highlight = cell * 0.12f
        scope.drawRect(
            color = Color.White.copy(alpha = 0.3f * alpha),
            topLeft = Offset(x + inset + highlight * 0.4f, y + inset + highlight * 0.4f),
            size = Size(side - highlight * 0.8f, highlight * 0.42f),
        )
        scope.drawRect(
            color = Color.White.copy(alpha = 0.14f * alpha),
            topLeft = Offset(x + inset + highlight * 0.4f, y + inset + highlight * 0.4f),
            size = Size(highlight * 0.42f, side - highlight * 0.8f),
        )
        scope.drawRect(
            color = Color.Black.copy(alpha = 0.32f * alpha),
            topLeft = Offset(x + inset + highlight * 0.4f, y + cell - inset - highlight),
            size = Size(side - highlight * 0.8f, highlight * 0.58f),
        )
        scope.drawRect(
            color = Color.Black.copy(alpha = 0.2f * alpha),
            topLeft = Offset(x + cell - inset - highlight, y + inset + highlight * 0.4f),
            size = Size(highlight * 0.58f, side - highlight * 0.8f),
        )
    }

    private fun drawGhost(
        scope: DrawScope,
        x: Float,
        y: Float,
        cell: Float,
        color: Color,
    ) {
        val inset = cell * 0.12f
        val side = cell - inset * 2f
        scope.drawRect(
            color = color.copy(alpha = 0.11f),
            topLeft = Offset(x + inset, y + inset),
            size = Size(side, side),
        )
        val stroke = maxOf(1f, cell * 0.035f)
        val ghost = color.copy(alpha = 0.42f)
        scope.drawRect(ghost, Offset(x + inset, y + inset), Size(side, stroke))
        scope.drawRect(ghost, Offset(x + inset, y + cell - inset - stroke), Size(side, stroke))
        scope.drawRect(ghost, Offset(x + inset, y + inset), Size(stroke, side))
        scope.drawRect(ghost, Offset(x + cell - inset - stroke, y + inset), Size(stroke, side))
    }
}
