package com.example.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

object SoundManager {
    private val scope = CoroutineScope(Dispatchers.Default)
    private const val SAMPLE_RATE = 44100

    private val _isSoundEnabled = MutableStateFlow(true)
    val isSoundEnabled = _isSoundEnabled.asStateFlow()

    fun toggleSound(): Boolean {
        _isSoundEnabled.value = !_isSoundEnabled.value
        if (_isSoundEnabled.value) {
            playCoin()
        }
        return _isSoundEnabled.value
    }

    fun setSoundEnabled(enabled: Boolean) {
        _isSoundEnabled.value = enabled
    }

    private fun playTones(tones: List<ToneSegment>, baseVolume: Float = 0.5f) {
        if (!_isSoundEnabled.value) return
        scope.launch {
            try {
                var totalSamples = 0
                tones.forEach { totalSamples += (it.durationMs * SAMPLE_RATE / 1000) }
                if (totalSamples == 0) return@launch

                val buffer = ShortArray(totalSamples)
                var currentIdx = 0

                tones.forEach { seg ->
                    val segSamples = seg.durationMs * SAMPLE_RATE / 1000
                    for (i in 0 until segSamples) {
                        val t = i.toDouble() / SAMPLE_RATE
                        // Smooth attack and release envelope to prevent clicks
                        val env = when {
                            i < segSamples * 0.1 -> (i / (segSamples * 0.1)).toFloat()
                            i > segSamples * 0.7 -> ((segSamples - i) / (segSamples * 0.3)).toFloat().coerceAtLeast(0f)
                            else -> 1.0f
                        }

                        // Linear pitch bend support
                        val freq = seg.startFreq + (seg.endFreq - seg.startFreq) * (i.toDouble() / segSamples)
                        val angle = 2.0 * PI * freq * t
                        val sampleVal = (sin(angle) * Short.MAX_VALUE * baseVolume * env * seg.volume).toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())

                        if (currentIdx < totalSamples) {
                            buffer[currentIdx++] = sampleVal.toShort()
                        }
                    }
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(totalSamples * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, totalSamples)
                track.play()

                // Small delay then release
                kotlinx.coroutines.delay((totalSamples * 1000L / SAMPLE_RATE) + 50)
                track.stop()
                track.release()
            } catch (_: Exception) {
                // Audio failure ignored gracefully
            }
        }
    }

    // --- CUTE GAME SOUND EFFECTS ---

    /** Crisp button/tap bubble pop */
    fun playTap() {
        playTones(
            listOf(ToneSegment(startFreq = 480.0, endFreq = 920.0, durationMs = 45, volume = 0.4f)),
            baseVolume = 0.4f
        )
    }

    /** Sparkling gold coin twin-bell chime */
    fun playCoin() {
        playTones(
            listOf(
                ToneSegment(startFreq = 987.77, endFreq = 987.77, durationMs = 70, volume = 0.6f),
                ToneSegment(startFreq = 1318.51, endFreq = 1318.51, durationMs = 150, volume = 0.7f)
            ),
            baseVolume = 0.6f
        )
    }

    /** Feeding yummy munch/crunch dual tone */
    fun playEat() {
        playTones(
            listOf(
                ToneSegment(startFreq = 340.0, endFreq = 520.0, durationMs = 50, volume = 0.5f),
                ToneSegment(startFreq = 280.0, endFreq = 440.0, durationMs = 60, volume = 0.6f),
                ToneSegment(startFreq = 380.0, endFreq = 620.0, durationMs = 70, volume = 0.7f)
            ),
            baseVolume = 0.5f
        )
    }

    /** Petting happy warm purr/chirp arpeggio */
    fun playPet() {
        playTones(
            listOf(
                ToneSegment(startFreq = 523.25, endFreq = 523.25, durationMs = 60, volume = 0.5f),
                ToneSegment(startFreq = 659.25, endFreq = 659.25, durationMs = 60, volume = 0.6f),
                ToneSegment(startFreq = 783.99, endFreq = 783.99, durationMs = 80, volume = 0.7f),
                ToneSegment(startFreq = 1046.50, endFreq = 1046.50, durationMs = 120, volume = 0.8f)
            ),
            baseVolume = 0.5f
        )
    }

    /** Bath bubbly splash */
    fun playClean() {
        playTones(
            listOf(
                ToneSegment(startFreq = 600.0, endFreq = 1200.0, durationMs = 60, volume = 0.5f),
                ToneSegment(startFreq = 1100.0, endFreq = 700.0, durationMs = 70, volume = 0.6f),
                ToneSegment(startFreq = 800.0, endFreq = 1400.0, durationMs = 90, volume = 0.7f)
            ),
            baseVolume = 0.5f
        )
    }

    /** Triumphant Level-Up Fanfare */
    fun playLevelUp() {
        playTones(
            listOf(
                ToneSegment(startFreq = 523.25, endFreq = 523.25, durationMs = 90, volume = 0.6f),  // C5
                ToneSegment(startFreq = 659.25, endFreq = 659.25, durationMs = 90, volume = 0.7f),  // E5
                ToneSegment(startFreq = 783.99, endFreq = 783.99, durationMs = 90, volume = 0.7f),  // G5
                ToneSegment(startFreq = 987.77, endFreq = 987.77, durationMs = 90, volume = 0.8f),  // B5
                ToneSegment(startFreq = 1046.50, endFreq = 1046.50, durationMs = 240, volume = 0.9f) // C6
            ),
            baseVolume = 0.7f
        )
    }

    /** Cute jump/hop boing slide */
    fun playJump() {
        playTones(
            listOf(
                ToneSegment(startFreq = 260.0, endFreq = 640.0, durationMs = 110, volume = 0.6f)
            ),
            baseVolume = 0.5f
        )
    }

    /** Mini-game victory / win */
    fun playSuccess() {
        playTones(
            listOf(
                ToneSegment(startFreq = 587.33, endFreq = 587.33, durationMs = 80, volume = 0.6f),  // D5
                ToneSegment(startFreq = 739.99, endFreq = 739.99, durationMs = 80, volume = 0.7f),  // F#5
                ToneSegment(startFreq = 880.00, endFreq = 880.00, durationMs = 160, volume = 0.8f)  // A5
            ),
            baseVolume = 0.6f
        )
    }

    /** Game over or miss */
    fun playFail() {
        playTones(
            listOf(
                ToneSegment(startFreq = 330.0, endFreq = 220.0, durationMs = 120, volume = 0.6f),
                ToneSegment(startFreq = 220.0, endFreq = 140.0, durationMs = 180, volume = 0.5f)
            ),
            baseVolume = 0.5f
        )
    }

    /** Card match success */
    fun playMatch() {
        playTones(
            listOf(
                ToneSegment(startFreq = 880.0, endFreq = 880.0, durationMs = 60, volume = 0.6f),
                ToneSegment(startFreq = 1174.66, endFreq = 1174.66, durationMs = 120, volume = 0.7f)
            ),
            baseVolume = 0.6f
        )
    }
}

data class ToneSegment(
    val startFreq: Double,
    val endFreq: Double,
    val durationMs: Int,
    val volume: Float = 1.0f
)
