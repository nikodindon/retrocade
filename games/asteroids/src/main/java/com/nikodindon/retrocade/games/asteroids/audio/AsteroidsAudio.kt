package com.nikodindon.retrocade.games.asteroids.audio

import com.nikodindon.retrocade.core.audio.MusicScale
import com.nikodindon.retrocade.core.audio.MusicTrack
import com.nikodindon.retrocade.core.audio.Sfx
import com.nikodindon.retrocade.core.audio.Waveform

object AsteroidsSfx {
    val shoot = Sfx(
        waveform = Waveform.SQUARE,
        startFrequencyHz = 940f,
        endFrequencyHz = 620f,
        durationMs = 52,
        attackMs = 1f,
        decayMs = 13f,
        sustainLevel = 0.32f,
        releaseMs = 24f,
        volume = 0.16f,
    )

    val thrust = Sfx(
        waveform = Waveform.NOISE,
        startFrequencyHz = 180f,
        endFrequencyHz = 95f,
        durationMs = 110,
        attackMs = 4f,
        decayMs = 36f,
        sustainLevel = 0.2f,
        releaseMs = 55f,
        volume = 0.11f,
        noiseSeed = 0xA57E20D,
    )

    val explosionLarge = Sfx(
        waveform = Waveform.NOISE,
        startFrequencyHz = 220f,
        endFrequencyHz = 48f,
        durationMs = 390,
        attackMs = 2f,
        decayMs = 120f,
        sustainLevel = 0.4f,
        releaseMs = 210f,
        volume = 0.3f,
        noiseSeed = 0xB16B00B5,
    )

    val explosionMedium = Sfx(
        waveform = Waveform.NOISE,
        startFrequencyHz = 320f,
        endFrequencyHz = 78f,
        durationMs = 250,
        attackMs = 2f,
        decayMs = 75f,
        sustainLevel = 0.38f,
        releaseMs = 130f,
        volume = 0.25f,
        noiseSeed = 0x51A7C0DE,
    )

    val explosionSmall = Sfx(
        waveform = Waveform.NOISE,
        startFrequencyHz = 460f,
        endFrequencyHz = 140f,
        durationMs = 150,
        attackMs = 1f,
        decayMs = 45f,
        sustainLevel = 0.34f,
        releaseMs = 80f,
        volume = 0.2f,
        noiseSeed = 0x0BADF00D,
    )

    val saucerAppeared = Sfx(
        waveform = Waveform.TRIANGLE,
        startFrequencyHz = 280f,
        endFrequencyHz = 460f,
        durationMs = 150,
        volume = 0.14f,
    )

    val saucerDestroyed = Sfx(
        waveform = Waveform.TRIANGLE,
        startFrequencyHz = 560f,
        endFrequencyHz = 150f,
        durationMs = 310,
        attackMs = 2f,
        decayMs = 95f,
        sustainLevel = 0.48f,
        releaseMs = 160f,
        volume = 0.28f,
    )

    val shipHit = Sfx(
        waveform = Waveform.SAW,
        startFrequencyHz = 190f,
        endFrequencyHz = 42f,
        durationMs = 620,
        attackMs = 3f,
        decayMs = 190f,
        sustainLevel = 0.4f,
        releaseMs = 300f,
        volume = 0.28f,
    )

    val extraLife = Sfx(
        waveform = Waveform.SQUARE,
        startFrequencyHz = 660f,
        endFrequencyHz = 1_320f,
        durationMs = 270,
        attackMs = 2f,
        decayMs = 70f,
        sustainLevel = 0.58f,
        releaseMs = 140f,
        volume = 0.28f,
    )

    val wave = Sfx(
        waveform = Waveform.TRIANGLE,
        startFrequencyHz = 330f,
        endFrequencyHz = 660f,
        durationMs = 230,
        attackMs = 3f,
        decayMs = 70f,
        sustainLevel = 0.5f,
        releaseMs = 120f,
        volume = 0.24f,
    )

    val pause = Sfx(
        waveform = Waveform.SQUARE,
        startFrequencyHz = 330f,
        endFrequencyHz = 290f,
        durationMs = 55,
        volume = 0.18f,
    )

    val gameOver = Sfx(
        waveform = Waveform.SAW,
        startFrequencyHz = 260f,
        endFrequencyHz = 40f,
        durationMs = 720,
        attackMs = 4f,
        decayMs = 190f,
        sustainLevel = 0.4f,
        releaseMs = 360f,
        volume = 0.28f,
    )
}

object AsteroidsMusic {
    fun track(seed: Long): MusicTrack = MusicTrack(
        seed = seed xor 0x0A57E20D5L,
        bpm = 128,
        bars = 16,
        scale = MusicScale.MINOR_PENTATONIC,
        rootMidi = 41,
    )
}
