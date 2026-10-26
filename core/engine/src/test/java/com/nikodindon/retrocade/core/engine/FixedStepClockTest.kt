package com.nikodindon.retrocade.core.engine

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FixedStepClockTest {
    @Test
    fun `accumulates partial frames`() {
        val clock = FixedStepClock(stepMillis = 16)

        assertEquals(0, clock.advance(10))
        assertEquals(1, clock.advance(10))
        assertEquals(6, clock.pendingMillis())
    }

    @Test
    fun `large delta produces multiple steps`() {
        val clock = FixedStepClock(stepMillis = 16)

        assertEquals(3, clock.advance(50))
        assertEquals(2, clock.pendingMillis())
    }

    @Test
    fun `clamps giant frame deltas`() {
        val clock = FixedStepClock(stepMillis = 16, maxFrameDeltaMillis = 250)

        assertEquals(15, clock.advance(10_000))
        assertEquals(10, clock.pendingMillis())
    }

    @Test
    fun `reset discards pending time`() {
        val clock = FixedStepClock(stepMillis = 16)
        clock.advance(8)
        clock.reset()

        assertEquals(0, clock.pendingMillis())
        assertTrue(clock.interpolate() == 0f)
    }
}
