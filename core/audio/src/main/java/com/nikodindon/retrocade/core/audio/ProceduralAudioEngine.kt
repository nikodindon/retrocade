package com.nikodindon.retrocade.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Low-latency streaming synthesizer. One daemon thread mixes active [Sfx] voices
 * with the current music loop and writes 512-frame PCM chunks to an [AudioTrack].
 */
@Singleton
class ProceduralAudioEngine @Inject constructor(
    @ApplicationContext private val context: Context,
) : AudioController {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val pendingVoices = ConcurrentLinkedQueue<Voice>()
    private val engineLock = Any()

    @Volatile
    private var running = false

    @Volatile
    private var sfxEnabled = true

    @Volatile
    private var musicEnabled = true

    @Volatile
    private var sfxVolume = 1f

    @Volatile
    private var musicVolume = 1f

    @Volatile
    private var musicPcm: ShortArray? = null

    @Volatile
    private var musicGeneration = 0

    private var audioTrack: AudioTrack? = null

    override fun play(sfx: Sfx) {
        if (!sfxEnabled) return
        ensureStarted()
        if (pendingVoices.size < MAX_QUEUED_VOICES) {
            pendingVoices.add(Voice(sfx))
        }
    }

    override fun setSfxEnabled(enabled: Boolean) {
        sfxEnabled = enabled
        if (!enabled) {
            pendingVoices.clear()
        }
    }

    override fun setMusicEnabled(enabled: Boolean) {
        musicEnabled = enabled
    }

    override fun setSfxVolume(volume: Float) {
        sfxVolume = volume.coerceIn(0f, 1f)
    }

    override fun setMusicVolume(volume: Float) {
        musicVolume = volume.coerceIn(0f, 1f)
    }

    override fun playMusic(track: MusicTrack) {
        ensureStarted()
        musicPcm = null
        musicGeneration += 1
        scope.launch {
            val pcm = MusicGenerator.render(track)
            musicPcm = pcm
            musicGeneration += 1
        }
    }

    override fun stopMusic() {
        musicPcm = null
        musicGeneration += 1
    }

    fun release() {
        synchronized(engineLock) {
            running = false
            audioTrack?.runCatching {
                pause()
                flush()
                stop()
                release()
            }
            audioTrack = null
        }
    }

    private fun ensureStarted() {
        synchronized(engineLock) {
            if (running) return
            running = true
            Thread({ audioLoop() }, "retrocade-audio").apply {
                priority = Thread.MAX_PRIORITY
                isDaemon = true
                start()
            }
        }
    }

    private fun audioLoop() {
        val track = createTrack() ?: run {
            running = false
            return
        }
        audioTrack = track
        val buffer = ShortArray(CHUNK_FRAMES)
        val active = ArrayList<Voice>(MAX_ACTIVE_VOICES)
        var localGeneration = -1
        var musicPosition = 0

        try {
            track.play()
            while (running) {
                if (!sfxEnabled) active.clear()
                while (active.size < MAX_ACTIVE_VOICES) {
                    active.add(pendingVoices.poll() ?: break)
                }

                val pcm = musicPcm
                if (pcm != null && localGeneration != musicGeneration) {
                    localGeneration = musicGeneration
                    musicPosition = 0
                }

                for (frame in buffer.indices) {
                    var mixed = 0f
                    var index = 0
                    while (index < active.size) {
                        val voice = active[index]
                        if (voice.finished) {
                            active.removeAt(index)
                        } else {
                            mixed += voice.nextSample() * sfxVolume
                            index++
                        }
                    }

                    if (musicEnabled && pcm != null && pcm.isNotEmpty()) {
                        mixed += pcm[musicPosition] / 32768f * MUSIC_VOLUME * musicVolume
                        musicPosition += 1
                        if (musicPosition >= pcm.size) musicPosition = 0
                    }

                    buffer[frame] = (mixed.coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
                }

                val written = track.write(buffer, 0, buffer.size)
                if (written < 0) break
            }
        } catch (_: IllegalStateException) {
            // Audio can be unavailable (device policy, emulator without audio).
        } finally {
            running = false
            audioTrack = null
            runCatching {
                track.pause()
                track.flush()
                track.release()
            }
        }
    }

    private fun createTrack(): AudioTrack? {
        val minBuffer = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        if (minBuffer <= 0) return null
        return runCatching {
            val builder = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                )
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setBufferSizeInBytes(maxOf(minBuffer, CHUNK_FRAMES * 4))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
            }
            builder.build()
                .also {
                    if (it.state != AudioTrack.STATE_INITIALIZED) {
                        it.release()
                        return null
                    }
                }
        }.getOrNull()
    }

    private class Voice(private val sfx: Sfx) {
        private var elapsed = 0
        private var phase = 0.0
        private var noiseState = if (sfx.noiseSeed != 0L) sfx.noiseSeed else 0x2545F491L
        private val attackSamples = (sfx.attackMs / 1_000f * SAMPLE_RATE).toInt().coerceAtLeast(1)
        private val decaySamples = (sfx.decayMs / 1_000f * SAMPLE_RATE).toInt().coerceAtLeast(1)
        private val releaseSamples = (sfx.releaseMs / 1_000f * SAMPLE_RATE).toInt().coerceAtLeast(1)
        private val durationSamples = sfx.durationSamples
        private val sustain = sfx.sustainLevel.coerceIn(0.01f, 1f)
        private val endOfSustain = (durationSamples - releaseSamples).coerceAtLeast(attackSamples)

        val finished: Boolean get() = elapsed >= durationSamples

        fun nextSample(): Float {
            val progress = elapsed.toFloat() / durationSamples
            val frequency = sfx.startFrequencyHz +
                (sfx.endFrequencyHz - sfx.startFrequencyHz) * progress

            val envelope = when {
                elapsed < attackSamples -> elapsed.toFloat() / attackSamples
                elapsed < attackSamples + decaySamples ->
                    1f - (1f - sustain) * ((elapsed - attackSamples).toFloat() / decaySamples)
                elapsed >= endOfSustain ->
                    sustain * ((durationSamples - elapsed).toFloat() / releaseSamples).coerceIn(0f, 1f)
                else -> sustain
            }

            val wave = when (sfx.waveform) {
                Waveform.SQUARE -> if (phase < 0.5) 0.8f else -0.8f
                Waveform.TRIANGLE -> (1.0 - 4.0 * abs(phase - 0.5)).toFloat()
                Waveform.SAW -> (2.0 * phase - 1.0).toFloat()
                Waveform.NOISE -> {
                    noiseState = noiseState xor (noiseState shl 13)
                    noiseState = noiseState xor (noiseState ushr 7)
                    noiseState = noiseState xor (noiseState shl 17)
                    (noiseState and 0xFFFF).toFloat() / 32_767f - 1f
                }
            }

            phase += frequency / SAMPLE_RATE
            if (phase >= 1.0) phase -= 1.0
            elapsed += 1
            return wave * envelope * sfx.volume
        }
    }

    companion object {
        const val SAMPLE_RATE = 44_100
        private const val CHUNK_FRAMES = 512
        private const val MAX_ACTIVE_VOICES = 16
        private const val MAX_QUEUED_VOICES = 12
        private const val MUSIC_VOLUME = 0.55f
    }
}
