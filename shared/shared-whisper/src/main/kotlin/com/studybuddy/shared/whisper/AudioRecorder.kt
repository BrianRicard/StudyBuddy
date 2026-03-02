package com.studybuddy.shared.whisper

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

/**
 * Records 16kHz mono PCM audio suitable for whisper.cpp input.
 *
 * Uses [AudioRecord] (not MediaRecorder) for raw PCM access.
 * Accumulates samples in a [ShortArray] buffer while recording,
 * then converts to normalized float on stop.
 */
@Singleton
class AudioRecorder @Inject constructor() {

    companion object {
        const val SAMPLE_RATE = 16_000
        private const val CHANNEL = AudioFormat.CHANNEL_IN_MONO
        private const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
        private const val MAX_DURATION_SECONDS = 120
    }

    private var audioRecord: AudioRecord? = null
    private val sampleBuffer = mutableListOf<Short>()

    @Volatile
    private var currentRms: Float = 0f

    val isRecording: Boolean get() = audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING

    /**
     * Start recording audio. Runs on IO dispatcher, continuously reading samples
     * until [stopRecording] is called or coroutine is cancelled.
     */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    suspend fun startRecording() = withContext(Dispatchers.IO) {
        val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, ENCODING)
            .coerceAtLeast(SAMPLE_RATE * 2) // At least 1 second buffer

        val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL,
            ENCODING,
            bufferSize,
        )

        sampleBuffer.clear()
        audioRecord = recorder
        recorder.startRecording()

        val readBuffer = ShortArray(bufferSize / 2)
        val maxSamples = SAMPLE_RATE * MAX_DURATION_SECONDS

        while (isActive && recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            val read = recorder.read(readBuffer, 0, readBuffer.size)
            if (read > 0) {
                synchronized(sampleBuffer) {
                    for (i in 0 until read) {
                        if (sampleBuffer.size < maxSamples) {
                            sampleBuffer.add(readBuffer[i])
                        }
                    }
                }
                // Compute RMS for amplitude visualization
                var sumSq = 0.0
                for (i in 0 until read) {
                    val sample = readBuffer[i].toFloat() / Short.MAX_VALUE
                    sumSq += sample * sample
                }
                currentRms = sqrt(sumSq / read).toFloat()
            }
        }
    }

    /**
     * Stop recording and return the audio as normalized floats in [-1, 1].
     */
    fun stopRecording(): FloatArray {
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null

        val shorts: List<Short>
        synchronized(sampleBuffer) {
            shorts = sampleBuffer.toList()
            sampleBuffer.clear()
        }

        currentRms = 0f

        return FloatArray(shorts.size) { i ->
            shorts[i].toFloat() / Short.MAX_VALUE
        }
    }

    /**
     * Current RMS amplitude (0.0–1.0) for visual feedback.
     */
    fun getCurrentAmplitude(): Float = currentRms

    /**
     * Release all resources.
     */
    fun release() {
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        sampleBuffer.clear()
        currentRms = 0f
    }
}
