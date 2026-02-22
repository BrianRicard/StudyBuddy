package com.studybuddy.shared.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.studybuddy.core.common.locale.SupportedLocale
import com.studybuddy.core.domain.model.VoicePack
import com.studybuddy.core.domain.model.VoicePackStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow

@Singleton
class TtsManager @Inject constructor(@ApplicationContext private val context: Context) {
    private var tts: TextToSpeech? = null
    private val _state = MutableStateFlow<TtsState>(TtsState.Initializing)
    val state: StateFlow<TtsState> = _state.asStateFlow()

    fun initialize() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                _state.value = TtsState.Ready
            } else {
                _state.value = TtsState.Error("TTS initialization failed")
            }
        }
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                // State already set in speak()
            }

            override fun onDone(utteranceId: String?) {
                _state.value = TtsState.Ready
            }

            @Deprecated("Deprecated in API")
            override fun onError(utteranceId: String?) {
                _state.value = TtsState.Error("TTS playback error")
            }

            override fun onError(
                utteranceId: String?,
                errorCode: Int,
            ) {
                _state.value = TtsState.Error("TTS playback error: $errorCode")
            }
        })
    }

    fun speak(
        text: String,
        locale: Locale,
        speed: Float = SPEED_NORMAL,
    ) {
        val engine = tts ?: return
        engine.language = locale
        engine.setSpeechRate(speed)
        _state.value = TtsState.Speaking(text)
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
    }

    fun stop() {
        tts?.stop()
        _state.value = TtsState.Ready
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        _state.value = TtsState.Initializing
    }

    fun getInstalledVoices(): List<VoicePack> {
        val engine = tts ?: return emptyList()
        return SupportedLocale.entries.map { supportedLocale ->
            val available = try {
                engine.isLanguageAvailable(supportedLocale.javaLocale) >= TextToSpeech.LANG_AVAILABLE
            } catch (_: Exception) {
                false
            }
            VoicePack(
                id = supportedLocale.code,
                locale = supportedLocale.code,
                displayName = supportedLocale.displayName,
                sizeBytes = 0L,
                status = if (available) VoicePackStatus.INSTALLED else VoicePackStatus.NOT_INSTALLED,
            )
        }
    }

    fun isLocaleAvailable(locale: Locale): Boolean {
        val engine = tts ?: return false
        return try {
            engine.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE
        } catch (_: Exception) {
            false
        }
    }

    fun downloadVoice(locale: Locale): Flow<DownloadProgress> = flow {
        // Voice download is handled by the system TTS engine.
        // We emit progress updates based on the locale availability check.
        emit(DownloadProgress(locale = locale.language))
        val engine = tts
        if (engine == null) {
            emit(DownloadProgress(locale = locale.language, error = "TTS not initialized"))
            return@flow
        }
        // Trigger system voice data install intent
        val available = engine.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE
        if (available) {
            emit(DownloadProgress(locale = locale.language, isComplete = true))
        } else {
            emit(
                DownloadProgress(
                    locale = locale.language,
                    error = "Voice not available. Please install via system settings.",
                ),
            )
        }
    }

    companion object {
        const val SPEED_NORMAL = 1.0f
        const val SPEED_SLOW = 0.7f
    }
}
