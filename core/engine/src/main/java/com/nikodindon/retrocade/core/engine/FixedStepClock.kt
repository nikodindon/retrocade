package com.nikodindon.retrocade.core.engine

import kotlin.math.max

/**
 * Accumulator that turns variable frame deltas into a number of fixed simulation
 * steps. The remainder is kept for the next frame.
 */
class FixedStepClock(
    val stepMillis: Long = 16L,
    private val maxFrameDeltaMillis: Long = 250L,
) {
    init {
        require(stepMillis > 0) { "stepMillis must be positive" }
        require(maxFrameDeltaMillis >= stepMillis) { "max delta must be at least one step" }
    }

    private var accumulator = 0L

    /** Returns how many fixed steps should run for [frameDeltaMillis]. */
    fun advance(frameDeltaMillis: Long): Int {
        accumulator += frameDeltaMillis.coerceIn(0L, maxFrameDeltaMillis)
        val steps = (accumulator / stepMillis).toInt()
        accumulator -= steps.toLong() * stepMillis
        return steps
    }

    /** Drops accumulated time, for example when the game is unpaused. */
    fun reset() {
        accumulator = 0L
    }

    fun pendingMillis(): Long = accumulator

    fun interpolate(): Float = max(0f, accumulator.toFloat() / stepMillis)
}
