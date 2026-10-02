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

    // Audio Throttling & Priority System (Sections 8 & 9)
    private fun priority(type: SfxType): Int {
        return when (type) {
            SfxType.MERGE,
            SfxType.COMBO,
            SfxType.GAME_OVER,
            SfxType.MISSION_WIN -> 5

            SfxType.OVERCHARGE -> 4

            SfxType.DANGER -> 3

            SfxType.DROP -> 2

            SfxType.BOUNCE -> 1
        }
    }

    private var lastPlayedPriority: Int = 0
    private var lastPriorityTimestamp: Long = 0L
    private val HIGH_PRIORITY_WINDOW_MS = 180L
    private var lastCollisionSoundTimestamp: Long = 0L
    private val MIN_COLLISION_SOUND_INTERVAL_MS = 140L

    // Pentatonic scale for chain combos
    private val comboScaleRatios = floatArrayOf(
        1.0f,     // C4
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

        val now = System.currentTimeMillis()
        val currentPriority = priority(type)

        if (
            currentPriority < lastPlayedPriority &&
            now - lastPriorityTimestamp < HIGH_PRIORITY_WINDOW_MS
        ) {
            return
        }

        // 8. Strict throttling on collision sounds
        if (type == SfxType.BOUNCE) {
            // Global cooldown check: discard if within 140ms cooldown window
            if (now - lastCollisionSoundTimestamp < MIN_COLLISION_SOUND_INTERVAL_MS) {
                return // Discard audio spam
            }
            lastCollisionSoundTimestamp = now
        }

        lastPlayedPriority = currentPriority
        lastPriorityTimestamp = now

        audioScope.launch {
            try {
                when (type) {
                    SfxType.DROP -> playTone(startFreq = 320f, endFreq = 160f, durationMs = 80, volume = 0.35f)
                    SfxType.BOUNCE -> playTone(startFreq = 160f, endFreq = 200f, durationMs = 45, volume = 0.20f)
                    SfxType.MERGE -> {
                        val baseFreq = 380f * pitch
                        playTone(startFreq = baseFreq, endFreq = baseFreq * 1.35f, durationMs = 150, volume = 0.60f)
                    }
                    SfxType.COMBO -> {
                        val scaleIdx = (comboStep - 1).coerceIn(0, comboScaleRatios.size - 1)
                        val ratio = comboScaleRatios[scaleIdx]
                        val baseFreq = 440f * ratio
                        playTone(startFreq = baseFreq, endFreq = baseFreq * 1.25f, durationMs = 200, volume = 0.70f)
                    }
                    SfxType.DANGER -> playTone(startFreq = 800f, endFreq = 620f, durationMs = 120, volume = 0.45f)
                    SfxType.OVERCHARGE -> playTone(startFreq = 180f, endFreq = 980f, durationMs = 320, volume = 0.85f)
                    SfxType.GAME_OVER -> playTone(startFreq = 400f, endFreq = 120f, durationMs = 450, volume = 0.65f)
                    SfxType.MISSION_WIN -> playTone(startFreq = 523.25f, endFreq = 1046.50f, durationMs = 400, volume = 0.80f)
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

        Thread.sleep(durationMs.toLong() + 30)
        track.release()
    }
}
