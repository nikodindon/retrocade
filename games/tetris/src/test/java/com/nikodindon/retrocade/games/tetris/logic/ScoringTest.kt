package com.nikodindon.retrocade.games.tetris.logic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.math.abs

class ScoringTest {
    @Test
    fun `line values follow the guideline table`() {
        assertEquals(100, ScoreTracker().registerLineClear(1).points)
        assertEquals(300, ScoreTracker().registerLineClear(2).points)
        assertEquals(500, ScoreTracker().registerLineClear(3).points)
        assertEquals(800, ScoreTracker().registerLineClear(4).points)
    }

    @Test
    fun `line points are multiplied by the level at clear time`() {
        val tracker = ScoreTracker()
        repeat(10) { tracker.registerLineClear(1) }
        tracker.registerLineClear(0)

        assertEquals(2, tracker.level)
        val outcome = tracker.registerLineClear(1)
        assertEquals(2, outcome.levelBefore)
        assertEquals(200, outcome.points)
    }

    @Test
    fun `combo adds a cumulative bonus and resets on a blank lock`() {
        val tracker = ScoreTracker()

        assertEquals(100, tracker.registerLineClear(1).points)
        assertEquals(150, tracker.registerLineClear(1).points)
        tracker.registerLineClear(0)
        assertEquals(100, tracker.registerLineClear(1).points)
    }

    @Test
    fun `consecutive tetrises apply back to back and combo bonuses`() {
        val tracker = ScoreTracker()
        val first = tracker.registerLineClear(4)
        val second = tracker.registerLineClear(4)

        assertFalse(first.backToBackApplied)
        assertTrue(second.backToBackApplied)
        assertEquals(800, first.points)
        assertEquals(1_250, second.points) // 800 * 1.5 + 50 combo
    }

    @Test
    fun `back to back survives a blank lock`() {
        val tracker = ScoreTracker()
        tracker.registerLineClear(4)
        tracker.registerLineClear(0)
        val second = tracker.registerLineClear(4)

        assertTrue(second.backToBackApplied)
        assertEquals(1_200, second.points)
    }

    @Test
    fun `a non tetris clear breaks back to back`() {
        val tracker = ScoreTracker()
        tracker.registerLineClear(4)
        tracker.registerLineClear(1)
        val third = tracker.registerLineClear(4)

        assertFalse(third.backToBackApplied)
    }

    @Test
    fun `soft and hard drop award one and two points per cell`() {
        val tracker = ScoreTracker()
        tracker.addSoftDrop(5)
        tracker.addHardDrop(4)

        assertEquals(13, tracker.score)
    }

    @Test
    fun `level advances every ten lines`() {
        val tracker = ScoreTracker()
        assertEquals(1, tracker.level)
        repeat(9) { tracker.registerLineClear(1) }
        assertEquals(1, tracker.level)
        tracker.registerLineClear(1)
        assertEquals(2, tracker.level)
        repeat(10) { tracker.registerLineClear(1) }
        assertEquals(3, tracker.level)
    }

    @Test
    fun `gravity formula matches the guideline values`() {
        assertEquals(1_000.0, TetrisTiming.gravityIntervalMs(1), 0.001)
        assertEquals(793.0, TetrisTiming.gravityIntervalMs(2), 0.5)
        assertEquals(617.8, TetrisTiming.gravityIntervalMs(3), 0.5)
        assertTrue(TetrisTiming.gravityIntervalMs(30) >= 1.0)
        assertTrue(abs(TetrisTiming.gravityIntervalMs(99) - 1.0) < 0.001)
    }
}
