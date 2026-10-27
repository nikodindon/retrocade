package com.nikodindon.retrocade.core.audio

enum class Waveform {
    SQUARE,
    TRIANGLE,
    SAW,
    NOISE,
}

/**
 * A synthetic sound description. There is no file: the audio engine renders the
 * waveform described here into PCM samples on the fly.
 */
data class Sfx(
    val waveform: Waveform,
    val startFrequencyHz: Float,
    val durationMs: Int,
    val endFrequencyHz: Float = startFrequencyHz,
    val attackMs: Float = 3f,
    val decayMs: Float = 20f,
    val sustainLevel: Float = 0.72f,
    val releaseMs: Float = 35f,
    val volume: Float = 0.45f,
    val noiseSeed: Long = 0L,
) {
    init {
        require(startFrequencyHz > 0f) { "frequency must be positive" }
        require(durationMs > 0) { "duration must be positive" }
        require(volume in 0f..1f) { "volume must be in 0..1" }
    }

    val durationSamples: Int
        get() = durationMs * ProceduralAudioEngine.SAMPLE_RATE / 1_000
}

interface SfxPlayer {
    fun play(sfx: Sfx)
}

interface AudioController : SfxPlayer {
    fun setSfxEnabled(enabled: Boolean)

    fun setMusicEnabled(enabled: Boolean)

    /** 0..1 linear volume. Out-of-range values are clamped by the implementation. */
    fun setSfxVolume(volume: Float)

    /** 0..1 linear volume. Out-of-range values are clamped by the implementation. */
    fun setMusicVolume(volume: Float)

    fun playMusic(track: MusicTrack)

    fun stopMusic()
}
