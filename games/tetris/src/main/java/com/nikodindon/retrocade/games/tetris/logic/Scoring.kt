package com.nikodindon.retrocade.games.tetris.logic

import kotlin.math.pow

object TetrisTiming {
    const val LOCK_DELAY_MS = 500L
    const val MAX_LOCK_RESETS = 15
    const val DAS_MS = 170L
    const val ARR_MS = 50L
    const val SOFT_DROP_INTERVAL_MS = 50L
    const val PREVIEW_COUNT = 3
    const val LINES_PER_LEVEL = 10

    /**
     * Guideline gravity formula, in milliseconds per row:
     * `(0.8 - (level - 1) * 0.007) ^ (level - 1)` seconds.
     */
    fun gravityIntervalMs(level: Int): Double {
        val safeLevel = level.coerceAtLeast(1)
        val seconds = (0.8 - (safeLevel - 1) * 0.007)
            .coerceAtLeast(0.05)
            .pow(safeLevel - 1)
        return (seconds * 1_000.0).coerceAtLeast(1.0)
    }
}

data class LineClearOutcome(
    val lines: Int,
    val points: Int,
    val combo: Int,
    val backToBackApplied: Boolean,
    val levelBefore: Int,
    val levelAfter: Int,
) {
    val isTetris: Boolean get() = lines == 4
}

/** Scoring state, kept free of Android so it can be tested exhaustively. */
class ScoreTracker {
    var score: Int = 0
        private set
    var lines: Int = 0
        private set
    var level: Int = 1
        private set
    var combo: Int = -1
        private set
    var backToBack: Boolean = false
        private set

    fun addSoftDrop(cells: Int) {
        if (cells > 0) score += cells * SOFT_DROP_POINT
    }

    fun addHardDrop(cells: Int) {
        if (cells > 0) score += cells * HARD_DROP_POINT
    }

    fun registerLineClear(count: Int): LineClearOutcome {
        require(count in 0..4) { "invalid line count $count" }
        val levelBefore = level
        if (count == 0) {
            combo = -1
            return LineClearOutcome(0, 0, combo, false, levelBefore, level)
        }

        combo += 1
        val b2bApplied = count == 4 && backToBack
        val base = LINE_POINTS[count] * levelBefore
        val clearPoints = if (b2bApplied) (base * 1.5f).toInt() else base
        val comboBonus = if (combo > 0) COMBO_POINT * combo * levelBefore else 0
        score += clearPoints + comboBonus
        lines += count
        level = lines / TetrisTiming.LINES_PER_LEVEL + 1
        backToBack = when (count) {
            4 -> true
            else -> false
        }
        return LineClearOutcome(
            lines = count,
            points = clearPoints + comboBonus,
            combo = combo,
            backToBackApplied = b2bApplied,
            levelBefore = levelBefore,
            levelAfter = level,
        )
    }

    companion object {
        val LINE_POINTS = intArrayOf(0, 100, 300, 500, 800)
        const val SOFT_DROP_POINT = 1
        const val HARD_DROP_POINT = 2
        const val COMBO_POINT = 50
    }
}
