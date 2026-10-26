package com.nikodindon.retrocade.core.audio

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.math.abs

class MusicGeneratorTest {
    private val track = MusicTrack(seed = 1234L, bpm = 120, bars = 2)

    @Test
    fun `loop length matches tempo and bars`() {
        val samples = MusicGenerator.render(track, sampleRate = 8_000)
        // 2 bars * 4 beats * 60 / 120 = 4 seconds.
        assertEquals(32_000, samples.size)
    }

    @Test
    fun `same seed is deterministic`() {
        val first = MusicGenerator.render(track, sampleRate = 8_000)
        val second = MusicGenerator.render(track.copy(), sampleRate = 8_000)
        assertTrue(first.contentEquals(second))
    }

    @Test
    fun `different seeds produce different music`() {
        val first = MusicGenerator.render(track, sampleRate = 8_000)
        val second = MusicGenerator.render(track.copy(seed = 99L), sampleRate = 8_000)
        assertNotEquals(first.toList(), second.toList())
    }

    @Test
    fun `audio is audible and does not clip`() {
        val samples = MusicGenerator.render(track, sampleRate = 8_000)
        val peak = samples.maxOf { abs(it.toInt()) }
        assertTrue(peak > 1_000, "expected audible output, peak=$peak")
        assertTrue(peak <= Short.MAX_VALUE)
    }
}
