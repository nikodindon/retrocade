package com.nikodindon.retrocade.games.tetris.audio

import com.nikodindon.retrocade.core.audio.MusicScale
import com.nikodindon.retrocade.core.audio.MusicTrack
import com.nikodindon.retrocade.core.audio.Sfx
import com.nikodindon.retrocade.core.audio.Waveform

object TetrisSfx {
    val move = Sfx(
        waveform = Waveform.SQUARE,
        startFrequencyHz = 240f,
        endFrequencyHz = 210f,
        durationMs = 34,
        attackMs = 1f,
        decayMs = 8f,
        sustainLevel = 0.45f,
        releaseMs = 18f,
        volume = 0.18f,
    )

    val rotate = Sfx(
        waveform = Waveform.SQUARE,
        startFrequencyHz = 420f,
        endFrequencyHz = 620f,
        durationMs = 46,
        attackMs = 1f,
        decayMs = 10f,
        sustainLevel = 0.5f,
        releaseMs = 22f,
        volume = 0.2f,
    )

    val lock = Sfx(
        waveform = Waveform.TRIANGLE,
        startFrequencyHz = 150f,
        endFrequencyHz = 95f,
        durationMs = 70,
        attackMs = 1f,
        decayMs = 24f,
        sustainLevel = 0.5f,
        releaseMs = 30f,
        volume = 0.3f,
    )

    val hardDrop = Sfx(
        waveform = Waveform.NOISE,
        startFrequencyHz = 900f,
        endFrequencyHz = 120f,
        durationMs = 90,
        attackMs = 1f,
        decayMs = 30f,
        sustainLevel = 0.25f,
        releaseMs = 45f,
        volume = 0.24f,
        noiseSeed = 0xC0FFEE,
    )

    val lineClear = Sfx(
        waveform = Waveform.SQUARE,
        startFrequencyHz = 520f,
        endFrequencyHz = 980f,
        durationMs = 150,
        attackMs = 2f,
        decayMs = 40f,
        sustainLevel = 0.6f,
        releaseMs = 70f,
        volume = 0.28f,
    )

    val tetris = Sfx(
        waveform = Waveform.SQUARE,
        startFrequencyHz = 620f,
        endFrequencyHz = 1_760f,
        durationMs = 320,
        attackMs = 2f,
        decayMs = 90f,
        sustainLevel = 0.62f,
        releaseMs = 130f,
        volume = 0.32f,
    )

    val levelUp = Sfx(
        waveform = Waveform.TRIANGLE,
        startFrequencyHz = 523f,
        endFrequencyHz = 1_046f,
        durationMs = 260,
        attackMs = 4f,
        decayMs = 70f,
        sustainLevel = 0.55f,
        releaseMs = 140f,
        volume = 0.28f,
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
        startFrequencyHz = 330f,
        endFrequencyHz = 55f,
        durationMs = 800,
        attackMs = 4f,
        decayMs = 180f,
        sustainLevel = 0.4f,
        releaseMs = 380f,
        volume = 0.28f,
    )
}

object TetrisMusic {
    fun track(seed: Long): MusicTrack = MusicTrack(
        seed = seed xor 0x7E7C15L,
        bpm = 116,
        bars = 16,
        scale = MusicScale.DORIAN,
        rootMidi = 45,
    )
}
