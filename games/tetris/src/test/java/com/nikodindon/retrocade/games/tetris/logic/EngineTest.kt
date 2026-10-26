package com.nikodindon.retrocade.games.tetris.logic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EngineTest {
    @Test
    fun `T wall kick uses the first free SRS offset`() {
        val engine = TetrisEngine(1L)
        engine.board.set(8, 4, 1)
        engine.forcePiece(PieceType.T, rotation = 0, x = 7, y = 2)

        assertTrue(engine.rotate(1))
        assertEquals(6, engine.current?.x)
        assertEquals(2, engine.current?.y)
        assertEquals(1, engine.current?.rotation)
    }

    @Test
    fun `I wall kick uses the I table`() {
        val engine = TetrisEngine(1L)
        engine.board.set(5, 2, 1)
        engine.forcePiece(PieceType.I, rotation = 0, x = 3, y = 2)

        assertTrue(engine.rotate(1))
        assertEquals(1, engine.current?.x)
        assertEquals(1, engine.current?.rotation)
    }

    @Test
    fun `rotation fails when every kick collides`() {
        val engine = TetrisEngine(1L)
        for (y in 0 until BOARD_HEIGHT) {
            for (x in 0 until BOARD_WIDTH) {
                engine.board.set(x, y, 1)
            }
        }
        engine.forcePiece(PieceType.T, rotation = 0, x = 3, y = 2)

        assertFalse(engine.rotate(1))
        assertEquals(0, engine.current?.rotation)
    }

    @Test
    fun `horizontal movement respects walls`() {
        val engine = TetrisEngine(1L)
        engine.forcePiece(PieceType.T, x = 0, y = 2)

        assertFalse(engine.moveHorizontal(-1))
        assertEquals(0, engine.current?.x)
        repeat(20) { engine.moveHorizontal(1) }
        assertEquals(7, engine.current?.x)
    }

    @Test
    fun `soft drop awards one point per cell`() {
        val engine = TetrisEngine(1L)
        engine.forcePiece(PieceType.O, x = 4, y = 0)

        engine.setSoftDrop(true)
        engine.update(TetrisTiming.SOFT_DROP_INTERVAL_MS)

        assertEquals(1, engine.current?.y)
        assertEquals(1, engine.tracker.score)
    }

    @Test
    fun `hard drop awards two points per cell and locks immediately`() {
        val engine = TetrisEngine(1L)
        engine.forcePiece(PieceType.O, x = 4, y = 0)
        var dropped = 0
        engine.eventSink = { if (it is TetrisEvent.HardDropped) dropped = it.cells }

        engine.hardDrop()

        assertEquals(20, dropped)
        assertEquals(40, engine.tracker.score)
        assertEquals(4, engine.board.filledCellCount())
        assertNotNull(engine.current)
    }

    @Test
    fun `gravity waits for the level interval`() {
        val engine = TetrisEngine(1L)
        engine.forcePiece(PieceType.O, x = 4, y = 0)

        engine.update(999)
        assertEquals(0, engine.current?.y)
        engine.update(1)
        assertEquals(1, engine.current?.y)
    }

    @Test
    fun `DAS and ARR drive horizontal auto repeat`() {
        val engine = TetrisEngine(1L)
        engine.forcePiece(PieceType.T, x = 0, y = 2)

        engine.onHorizontalHold(1, true)
        assertEquals(1, engine.current?.x)

        engine.update(169)
        assertEquals(1, engine.current?.x)
        engine.update(1)
        assertEquals(2, engine.current?.x)
        engine.update(TetrisTiming.ARR_MS)
        assertEquals(3, engine.current?.x)

        engine.onHorizontalHold(1, false)
        engine.update(200)
        assertEquals(3, engine.current?.x)
    }

    @Test
    fun `lock delay is 500 ms`() {
        val engine = TetrisEngine(1L)
        engine.forcePiece(PieceType.O, x = 4, y = 20)

        engine.update(499)
        assertEquals(0, engine.board.filledCellCount())
        engine.update(1)

        assertEquals(4, engine.board.filledCellCount())
    }

    @Test
    fun `lock delay resets are capped at fifteen per piece`() {
        val engine = TetrisEngine(1L)
        engine.forcePiece(PieceType.O, x = 4, y = 20)

        repeat(15) {
            engine.rotate(1)
            engine.update(100)
        }
        assertEquals(15, engine.remainingLockResets())
        assertEquals(0, engine.board.filledCellCount())

        var locked = false
        repeat(10) {
            if (!locked) {
                engine.rotate(1)
                engine.update(100)
                locked = engine.board.filledCellCount() > 0
            }
        }

        assertTrue(locked, "piece should lock after the reset budget is exhausted")
    }

    @Test
    fun `clears one to four lines and scores them`() {
        listOf(1, 2, 3, 4).forEach { count ->
            val engine = TetrisEngine(1L)
            for (y in (BOARD_HEIGHT - count) until BOARD_HEIGHT) {
                for (x in 0 until BOARD_WIDTH - 1) {
                    engine.board.set(x, y, 1)
                }
            }
            engine.forcePiece(PieceType.I, rotation = 1, x = 7, y = 0)
            var cleared = 0
            engine.eventSink = { if (it is TetrisEvent.LineCleared) cleared = it.lines }

            engine.hardDrop()

            assertEquals(count, cleared)
            assertEquals(count, engine.tracker.lines)
            assertEquals(18 * 2 + ScoreTracker.LINE_POINTS[count], engine.tracker.score)
        }
    }

    @Test
    fun `game over occurs when a new piece cannot spawn`() {
        val engine = TetrisEngine(1L)
        engine.board.set(4, 0, 1)
        var gameOverScore = -1
        engine.eventSink = { if (it is TetrisEvent.GameOver) gameOverScore = it.score }

        engine.forceSpawn(PieceType.O)

        assertTrue(engine.gameOver)
        assertNull(engine.current)
        assertEquals(engine.tracker.score, gameOverScore)
    }

    @Test
    fun `hold swaps once per placement`() {
        val engine = TetrisEngine(1L)
        val firstType = engine.current?.type
        assertNotNull(firstType)

        engine.hold()
        assertEquals(firstType, engine.holdPiece)
        assertFalse(engine.canHold)
        val secondType = engine.current?.type

        engine.hold()
        assertEquals(secondType, engine.current?.type)
        assertEquals(firstType, engine.holdPiece)

        engine.hardDrop()
        assertTrue(engine.canHold)
        engine.hold()
        assertEquals(firstType, engine.current?.type)
    }

    @Test
    fun `events are emitted for moves rotations and locks`() {
        val engine = TetrisEngine(1L)
        val events = mutableListOf<TetrisEvent>()
        engine.eventSink = { events += it }
        engine.forcePiece(PieceType.T, x = 3, y = 2)

        engine.moveHorizontal(1)
        engine.rotate(1)
        engine.hardDrop()

        assertTrue(events.any { it is TetrisEvent.Moved })
        assertTrue(events.any { it is TetrisEvent.Rotated })
        assertTrue(events.any { it is TetrisEvent.PieceLocked })
        assertTrue(events.any { it is TetrisEvent.HardDropped })
    }

    @Test
    fun `two engines seeded identically play identically`() {
        val first = TetrisEngine(314L)
        val second = TetrisEngine(314L)

        repeat(10) {
            first.moveHorizontal(1)
            second.moveHorizontal(1)
            first.update(16)
            second.update(16)
        }
        first.hardDrop()
        second.hardDrop()

        assertEquals(first.tracker.score, second.tracker.score)
        assertTrue(first.board.copyCells().contentEquals(second.board.copyCells()))
        assertEquals(first.preview, second.preview)
    }
}
