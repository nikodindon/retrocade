package com.nikodindon.retrocade.games.tetris.logic

import com.nikodindon.retrocade.core.common.Rng
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PieceBagTest {
    @Test
    fun `every group of seven contains all pieces exactly once`() {
        val bag = PieceBag(Rng(17L))
        repeat(20) {
            val group = List(7) { bag.next() }
            assertEquals(PieceType.entries.toSet(), group.toSet())
            assertEquals(7, group.size)
        }
    }

    @Test
    fun `same seed gives the same draw order`() {
        val first = PieceBag(Rng(123L))
        val second = PieceBag(Rng(123L))
        repeat(50) {
            assertEquals(first.next(), second.next())
        }
    }

    @Test
    fun `different seeds give different bags`() {
        val firstBag = PieceBag(Rng(1L))
        val secondBag = PieceBag(Rng(2L))
        val first = List(14) { firstBag.next() }
        val second = List(14) { secondBag.next() }
        assertNotEquals(first, second)
    }

    @Test
    fun `peek does not consume the queue`() {
        val bag = PieceBag(Rng(9L))
        val first = bag.peek(5)
        val second = bag.peek(5)

        assertEquals(first, second)
        assertEquals(first.first(), bag.next())
    }

    @Test
    fun `engines with the same seed expose the same preview and first piece`() {
        val first = TetrisEngine(2024L)
        val second = TetrisEngine(2024L)

        assertEquals(first.preview, second.preview)
        assertEquals(first.current?.type, second.current?.type)
    }

    @Test
    fun `preview helper returns a deterministic sequence`() {
        val first = TetrisEngine.previewForSeed(15L, 14)
        val second = TetrisEngine.previewForSeed(15L, 14)
        assertEquals(first, second)
        first.chunked(7).forEach { chunk ->
            assertTrue(chunk.toSet().size == 7)
        }
    }
}
