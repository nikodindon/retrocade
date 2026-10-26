package com.nikodindon.retrocade.core.audio

import com.nikodindon.retrocade.core.common.Rng
import kotlin.math.pow
import kotlin.math.roundToInt

enum class MusicScale(val intervals: IntArray) {
    NATURAL_MINOR(intArrayOf(0, 2, 3, 5, 7, 8, 10)),
    DORIAN(intArrayOf(0, 2, 3, 5, 7, 9, 10)),
    MINOR_PENTATONIC(intArrayOf(0, 3, 5, 7, 10)),
}

/**
 * A deterministic chiptune loop. The same seed always produces the same track.
 */
data class MusicTrack(
    val seed: Long,
    val bpm: Int = 112,
    val bars: Int = 16,
    val beatsPerBar: Int = 4,
    val scale: MusicScale = MusicScale.NATURAL_MINOR,
    val rootMidi: Int = 45,
)

/**
 * Renders a complete music loop as mono 16-bit PCM at 44.1 kHz.
 *
 * The whole loop is generated once and then streamed by the audio engine, which
 * keeps playback seamless. Note tails wrap around the loop boundary so there is
 * no click at the seam.
 */
object MusicGenerator {
    const val SAMPLE_RATE = 44_100

    fun render(track: MusicTrack, sampleRate: Int = SAMPLE_RATE): ShortArray {
        require(track.bars > 0) { "bars must be positive" }
        require(track.bpm in 40..240) { "bpm out of range" }

        val beatSeconds = 60.0 / track.bpm
        val totalSeconds = beatSeconds * track.beatsPerBar * track.bars
        val totalSamples = (totalSeconds * sampleRate).roundToInt().coerceAtLeast(1)
        val buffer = FloatArray(totalSamples)
        val rng = Rng(track.seed)
        val scale = track.scale.intervals
        val progression = progressionFor(rng)

        // Bass: root/fifth pulse on every beat.
        for (bar in 0 until track.bars) {
            val degree = progression[bar % progression.size]
            val root = midiToFrequency(track.rootMidi + scale[degree % scale.size])
            for (beat in 0 until track.beatsPerBar) {
                val start = ((bar * track.beatsPerBar + beat) * beatSeconds * sampleRate).toInt()
                val isOffBeat = beat % 2 == 1
                addNote(
                    buffer = buffer,
                    startSample = start,
                    sampleRate = sampleRate,
                    frequency = if (isOffBeat) root * 1.5f else root,
                    durationSamples = (beatSeconds * sampleRate * 0.82).toInt(),
                    waveform = Waveform.TRIANGLE,
                    volume = 0.22f,
                    attack = 0.004,
                    release = 0.14,
                )
            }
        }

        // Arpeggio: fast 16th-note chord tones, quiet and sparkly.
        val arpStepSamples = (beatSeconds * sampleRate / 4.0).toInt()
        for (bar in 0 until track.bars) {
            val degree = progression[bar % progression.size]
            val chord = intArrayOf(degree, degree + 2, degree + 4, degree + 2)
            for (step in 0 until track.beatsPerBar * 4) {
                if (rng.nextFloat() < 0.14f) {
                    continue
                }
                val noteDegree = chord[step % chord.size]
                val octave = if (step % 8 >= 4) 12 else 0
                val midi = track.rootMidi + 24 + octave + scale[noteDegree % scale.size]
                addNote(
                    buffer = buffer,
                    startSample = (bar * track.beatsPerBar * beatSeconds * sampleRate).toInt() +
                        step * arpStepSamples,
                    sampleRate = sampleRate,
                    frequency = midiToFrequency(midi),
                    durationSamples = (arpStepSamples * 0.7f).toInt(),
                    waveform = Waveform.SQUARE,
                    volume = 0.075f,
                    attack = 0.002,
                    release = 0.03,
                )
            }
        }

        // Lead: sparse seeded melody, one or two notes per bar.
        for (bar in 0 until track.bars) {
            val noteCount = 1 + rng.nextInt(3)
            for (note in 0 until noteCount) {
                if (rng.nextFloat() < 0.22f) continue
                val step = rng.nextInt(track.beatsPerBar * 2) / 2.0
                val degree = rng.nextInt(scale.size)
                val octave = if (rng.nextFloat() < 0.3f) 12 else 0
                val frequency = midiToFrequency(
                    track.rootMidi + 12 + octave + scale[degree],
                )
                addNote(
                    buffer = buffer,
                    startSample = ((bar * track.beatsPerBar + step) * beatSeconds * sampleRate).toInt(),
                    sampleRate = sampleRate,
                    frequency = frequency,
                    durationSamples = (beatSeconds * sampleRate * 0.9).toInt(),
                    waveform = Waveform.SQUARE,
                    volume = 0.09f,
                    attack = 0.008,
                    release = 0.12,
                )
            }
        }

        // Soft wrap-around delay for a little arcade space.
        val delaySamples = (beatSeconds * sampleRate * 0.75).toInt().coerceAtLeast(1)
        for (i in buffer.indices) {
            buffer[(i + delaySamples) % totalSamples] += buffer[i] * 0.18f
        }

        var peak = 0.001f
        for (sample in buffer) {
            val absolute = kotlin.math.abs(sample)
            if (absolute > peak) peak = absolute
        }
        val gain = (0.82f / peak).coerceAtMost(1.6f)

        return ShortArray(totalSamples) { index ->
            (buffer[index] * gain * Short.MAX_VALUE)
                .coerceIn(Short.MIN_VALUE.toFloat(), Short.MAX_VALUE.toFloat())
                .toInt()
                .toShort()
        }
    }

    private fun progressionFor(rng: Rng): IntArray {
        val options = listOf(
            intArrayOf(0, 5, 3, 4),
            intArrayOf(0, 3, 5, 4),
            intArrayOf(0, 4, 5, 3),
            intArrayOf(0, 5, 1, 4),
        )
        return options[rng.nextInt(options.size)]
    }

    private fun midiToFrequency(midi: Int): Float =
        (440.0 * 2.0.pow((midi - 69) / 12.0)).toFloat()

    private fun addNote(
        buffer: FloatArray,
        startSample: Int,
        sampleRate: Int,
        frequency: Float,
        durationSamples: Int,
        waveform: Waveform,
        volume: Float,
        attack: Double,
        release: Double,
    ) {
        if (durationSamples <= 0) return
        val length = buffer.size
        var phase = 0.0
        val attackSamples = (attack * sampleRate).toInt().coerceAtLeast(1)
        val releaseSamples = (release * sampleRate).toInt().coerceAtLeast(1)
        val holdSamples = (durationSamples - releaseSamples).coerceAtLeast(attackSamples)

        for (index in 0 until durationSamples) {
            val envelope = when {
                index < attackSamples -> index.toFloat() / attackSamples
                index < holdSamples -> 1f
                else -> ((durationSamples - index).toFloat() / releaseSamples).coerceIn(0f, 1f)
            }
            val sample = wave(waveform, phase, index) * envelope * volume
            buffer[(startSample + index) % length] += sample
            phase += frequency / sampleRate
            if (phase >= 1.0) phase -= 1.0
        }
    }

    private fun wave(waveform: Waveform, phase: Double, index: Int): Float = when (waveform) {
        Waveform.SQUARE -> if (phase < 0.5) 0.8f else -0.8f
        Waveform.TRIANGLE -> (1.0 - 4.0 * kotlin.math.abs(phase - 0.5)).toFloat()
        Waveform.SAW -> (2.0 * phase - 1.0).toFloat()
        Waveform.NOISE -> {
            val x = (index * 1_103_515_245 + 12_345) and 0x7fffffff
            ((x % 2001) - 1000) / 1000f
        }
    }
}
