package com.nikodindon.retrocade.core.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RngTest {
    @Test
    fun `same seed produces same sequence`() {
        val first = Rng(42L)
        val second = Rng(42L)

        repeat(100) {
            assertEquals(first.nextLong(), second.nextLong())
        }
    }

    @Test
    fun `different seeds produce different sequences`() {
        val first = Rng(1L)
        val second = Rng(2L)

        val a = List(8) { first.nextLong() }
        val b = List(8) { second.nextLong() }
        assertNotEquals(a, b)
    }

    @Test
    fun `bounded ints stay in range and cover every value`() {
        val rng = Rng(7L)
        val seen = mutableSetOf<Int>()
        repeat(2_000) {
            val value = rng.nextInt(7)
            assertTrue(value in 0..6)
            seen += value
        }
        assertEquals((0..6).toSet(), seen)
    }

    @Test
    fun `shuffle is deterministic and keeps every element`() {
        val source = (1..20).toList()
        val first = Rng(99L).shuffled(source)
        val second = Rng(99L).shuffled(source)

        assertEquals(first, second)
        assertEquals(source, first.sorted())
    }

    @Test
    fun `float is in unit interval`() {
        val rng = Rng(3L)
        repeat(1_000) {
            val value = rng.nextFloat()
            assertTrue(value >= 0f && value < 1f)
        }
    }
}
