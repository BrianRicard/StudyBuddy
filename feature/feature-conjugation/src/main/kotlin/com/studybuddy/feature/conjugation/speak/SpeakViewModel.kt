package com.studybuddy.feature.conjugation.speak

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.common.PronunciationChecker
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.common.constants.PointValues
import com.studybuddy.core.domain.model.PointSource
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson
import com.studybuddy.core.domain.model.conjugation.ConjugationStage
import com.studybuddy.core.domain.model.conjugation.ConjugationStages
import com.studybuddy.core.domain.model.conjugation.ConjugationStep
import com.studybuddy.core.domain.repository.ConjugationRepository
import com.studybuddy.core.domain.repository.SettingsRepository
import com.studybuddy.core.domain.usecase.points.AwardPointsUseCase
import com.studybuddy.shared.tts.TtsManager
import com.studybuddy.shared.whisper.AudioRecorder
import com.studybuddy.shared.whisper.ModelDownloadManager
import com.studybuddy.shared.whisper.WhisperEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlin.math.sqrt
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SpeakPhase {
    IDLE,
    RECORDING,
    PROCESSING,
    HEARD,
    ENCOURAGE,
}

data class SpeakState(
    val stage: ConjugationStage? = null,
    val index: Int = 0,
    val phase: SpeakPhase = SpeakPhase.IDLE,
    /** True when a whisper model is loaded — spoken words are actually scored. */
    val whisperScoring: Boolean = false,
    val hasAudioPermission: Boolean = false,
    /** True when mic permission was denied: explain it and offer a self-report tap. */
    val showPermissionHint: Boolean = false,
    val spokenCount: Int = 0,
    val attemptsOnCurrent: Int = 0,
    val isSaving: Boolean = false,
) {
    val person: ConjugationPerson get() = ConjugationPerson.entries[index]
    val total: Int get() = ConjugationPerson.entries.size
    val isLast: Boolean get() = index == total - 1
}

sealed interface SpeakIntent {
    data object PlayForm : SpeakIntent
    data object StartRecording : SpeakIntent
    data object StopRecording : SpeakIntent

    /** Self-report tap, only offered when mic permission was denied. */
    data object ConfirmWithoutMic : SpeakIntent

    /** Initial permission state read by the screen — never auto-starts recording. */
    data class PermissionSeeded(val granted: Boolean) : SpeakIntent

    /** Result of an in-flow permission request — starts recording when granted. */
    data class AudioPermissionResult(val granted: Boolean) : SpeakIntent
    data object Next : SpeakIntent
}

sealed interface SpeakEffect {
    data object RequestAudioPermission : SpeakEffect
    data object Completed : SpeakEffect
}

@HiltViewModel
class SpeakViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val ttsManager: TtsManager,
    private val whisperEngine: WhisperEngine,
    private val audioRecorder: AudioRecorder,
    private val modelDownloadManager: ModelDownloadManager,
    private val settingsRepository: SettingsRepository,
    private val conjugationRepository: ConjugationRepository,
    private val awardPointsUseCase: AwardPointsUseCase,
) : ViewModel() {

    private val stageId: String = checkNotNull(savedStateHandle["stageId"])
    private var recordingJob: Job? = null

    private val _state = MutableStateFlow(SpeakState(stage = ConjugationStages.byId(stageId)))
    val state: StateFlow<SpeakState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<SpeakEffect>()
    val effects: SharedFlow<SpeakEffect> = _effects.asSharedFlow()

    init {
        loadWhisperModelIfAvailable()
    }

    fun onIntent(intent: SpeakIntent) {
        when (intent) {
            is SpeakIntent.PlayForm -> playForm()
            is SpeakIntent.StartRecording -> startRecording()
            is SpeakIntent.StopRecording -> stopRecording()
            is SpeakIntent.ConfirmWithoutMic -> confirmWithoutMic()
            is SpeakIntent.PermissionSeeded ->
                _state.update { it.copy(hasAudioPermission = intent.granted) }

            is SpeakIntent.AudioPermissionResult -> onPermissionResult(intent.granted)
            is SpeakIntent.Next -> next()
        }
    }

    /** With a downloaded whisper model the spoken word is actually scored. */
    private fun loadWhisperModelIfAvailable() {
        viewModelScope.launch {
            val preferredFileName = settingsRepository.getWhisperModel().first()
            val model = modelDownloadManager.bestAvailableModel(preferredFileName) ?: return@launch
            val modelPath = modelDownloadManager.getModelPath(model) ?: return@launch
            whisperEngine.initialize(modelPath).onSuccess {
                _state.update { it.copy(whisperScoring = true) }
            }
        }
    }

    private fun playForm() {
        val stage = _state.value.stage ?: return
        ttsManager.speak(stage.verb.display(_state.value.person), Locale.FRENCH)
    }

    // Permission is verified via hasAudioPermission before reaching the call.
    @Suppress("MissingPermission")
    private fun startRecording() {
        val current = _state.value
        if (current.phase == SpeakPhase.RECORDING) return
        if (!current.hasAudioPermission) {
            viewModelScope.launch { _effects.emit(SpeakEffect.RequestAudioPermission) }
            return
        }
        ttsManager.stop()
        _state.update { it.copy(phase = SpeakPhase.RECORDING) }
        recordingJob = viewModelScope.launch { audioRecorder.startRecording() }
    }

    private fun stopRecording() {
        val current = _state.value
        val stage = current.stage ?: return
        if (current.phase != SpeakPhase.RECORDING) return

        recordingJob?.cancel()
        val samples = audioRecorder.stopRecording()
        _state.update { it.copy(phase = SpeakPhase.PROCESSING) }

        viewModelScope.launch {
            if (current.whisperScoring) {
                scoreWithWhisper(stage, current.person, samples)
            } else {
                // No scoring model: require that the child actually spoke, so a
                // silent tap can never pass, then accept generously.
                if (heardSpeech(samples)) {
                    onSpokenWell(PointValues.CONJUGATION_FORM_ECHOED)
                } else {
                    encourage()
                }
            }
        }
    }

    private suspend fun scoreWithWhisper(
        stage: ConjugationStage,
        person: ConjugationPerson,
        samples: FloatArray,
    ) {
        if (!heardSpeech(samples)) {
            encourage()
            return
        }
        val expected = stage.verb.display(person)
        val result = whisperEngine.transcribe(samples = samples, language = "fr", initialPrompt = expected)
        val similarity = result.getOrNull()
            ?.let { PronunciationChecker.similarity(expected, it.text) }
            ?: 0f
        if (similarity >= PASS_THRESHOLD) {
            onSpokenWell(PointValues.CONJUGATION_FORM_SPOKEN)
        } else {
            encourage()
        }
    }

    private fun encourage() {
        _state.update {
            it.copy(phase = SpeakPhase.ENCOURAGE, attemptsOnCurrent = it.attemptsOnCurrent + 1)
        }
    }

    private fun onSpokenWell(points: Int) {
        _state.update { it.copy(phase = SpeakPhase.HEARD, spokenCount = it.spokenCount + 1) }
        viewModelScope.launch {
            awardPointsUseCase(
                profileId = AppConstants.DEFAULT_PROFILE_ID,
                basePoints = points,
                streak = 0,
                source = PointSource.CONJUGATION,
                reason = "Conjugation spoken: $stageId",
            )
        }
    }

    /** Self-report path, only reachable when mic permission was denied. */
    private fun confirmWithoutMic() {
        val current = _state.value
        if (!current.showPermissionHint || current.phase == SpeakPhase.HEARD) return
        onSpokenWell(PointValues.CONJUGATION_FORM_ECHOED)
    }

    private fun onPermissionResult(granted: Boolean) {
        _state.update { it.copy(hasAudioPermission = granted, showPermissionHint = !granted) }
        if (granted) {
            // The child already tapped the mic — don't make them tap twice.
            startRecording()
        }
    }

    private fun next() {
        val current = _state.value
        if (current.phase != SpeakPhase.HEARD && current.phase != SpeakPhase.ENCOURAGE) return
        if (!current.isLast) {
            _state.update {
                it.copy(index = it.index + 1, phase = SpeakPhase.IDLE, attemptsOnCurrent = 0)
            }
            return
        }
        finish(current.spokenCount)
    }

    private fun finish(spokenCount: Int) {
        if (_state.value.isSaving) return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            conjugationRepository.recordStepResult(
                profileId = AppConstants.DEFAULT_PROFILE_ID,
                stageId = stageId,
                step = ConjugationStep.SPEAK,
                correct = spokenCount,
                total = _state.value.total,
            )
            _effects.emit(SpeakEffect.Completed)
        }
    }

    /** True when the recording actually contains speech (not silence or an instant tap). */
    private fun heardSpeech(samples: FloatArray): Boolean {
        if (samples.size < MIN_SPEECH_SAMPLES) return false
        var sumSquares = 0.0
        for (sample in samples) sumSquares += (sample * sample).toDouble()
        val rms = sqrt(sumSquares / samples.size)
        return rms >= SPEECH_RMS_THRESHOLD
    }

    override fun onCleared() {
        recordingJob?.cancel()
        audioRecorder.release()
        // Deliberately NOT releasing whisperEngine: it is a singleton shared
        // with the poems screen, and freeing the native context here could
        // race an in-flight transcription.
        ttsManager.stop()
        super.onCleared()
    }

    private companion object {
        /** Generous on purpose: hearing the child try matters more than precision. */
        const val PASS_THRESHOLD = 0.5f

        /** Minimum RMS loudness that counts as "the child spoke". */
        const val SPEECH_RMS_THRESHOLD = 0.015f

        /** At least ~0.3s of audio, so an instant start/stop tap never counts. */
        const val MIN_SPEECH_SAMPLES = AudioRecorder.SAMPLE_RATE * 3 / 10
    }
}
