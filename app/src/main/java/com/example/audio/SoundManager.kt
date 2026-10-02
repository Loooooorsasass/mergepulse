package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.game.engine.GameWorld.SfxType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class SoundManager {

    var isSoundEnabled: Boolean = true
    var isVibrationEnabled: Boolean = true

    private val sampleRate = 22050
    private val audioScope = CoroutineScope(Dispatchers.Default)

    // Musical Pentatonic Scale for Combo Chains (C4, D4, E4, G4, A4, C5, D5, E5)
    private val comboScaleRatios = floatArrayOf(
        1.0f,     // C4 (1.0x)
        1.122f,   // D4
        1.260f,   // E4
        1.498f,   // G4
        1.682f,   // A4
        2.0f,     // C5
        2.245f,   // D5
        2.520f    // E5
    )

    fun playSfx(type: SfxType, pitch: Float = 1.0f, comboStep: Int = 1) {
        if (!isSoundEnabled) return

        audioScope.launch {
            try {
                when (type) {
                    SfxType.DROP -> playTone(startFreq = 340f, endFreq = 140f, durationMs = 110, volume = 0.4f)
                    SfxType.BOUNCE -> playTone(startFreq = 180f, endFreq = 220f, durationMs = 50, volume = 0.25f)
                    SfxType.MERGE -> {
                        val baseFreq = 380f * pitch
                        playTone(startFreq = baseFreq, endFreq = baseFreq * 1.4f, durationMs = 160, volume = 0.55f)
                    }
                    SfxType.COMBO -> {
                        val scaleIdx = (comboStep - 1).coerceIn(0, comboScaleRatios.size - 1)
                        val ratio = comboScaleRatios[scaleIdx]
                        val baseFreq = 440f * ratio
                        playTone(startFreq = baseFreq, endFreq = baseFreq * 1.25f, durationMs = 220, volume = 0.75f)
                    }
                    SfxType.DANGER -> playTone(startFreq = 880f, endFreq = 660f, durationMs = 140, volume = 0.5f)
                    SfxType.OVERCHARGE -> playTone(startFreq = 150f, endFreq = 950f, durationMs = 380, volume = 0.85f)
                    SfxType.GAME_OVER -> playTone(startFreq = 420f, endFreq = 100f, durationMs = 550, volume = 0.7f)
                    SfxType.MISSION_WIN -> playTone(startFreq = 523.25f, endFreq = 1046.50f, durationMs = 450, volume = 0.8f)
                }
            } catch (e: Exception) {
                // Ignore audio playback exceptions gracefully
            }
        }
    }

    private fun playTone(startFreq: Float, endFreq: Float, durationMs: Int, volume: Float) {
        val numSamples = (durationMs * sampleRate) / 1000
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val currentFreq = startFreq + (endFreq - startFreq) * progress
            val angle = 2.0 * PI * currentFreq * i / sampleRate
            val envelope = 1.0 - progress
            val sampleValue = (sin(angle) * Short.MAX_VALUE * volume * envelope).toInt()
            buffer[i] = sampleValue.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(buffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(buffer, 0, buffer.size)
        track.play()

        Thread.sleep(durationMs.toLong() + 40)
        track.release()
    }
}
