package com.studybuddy.feature.poems

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.domain.model.Poem
import com.studybuddy.core.domain.model.ReadingSession
import com.studybuddy.core.domain.usecase.poem.GetPoemByIdUseCase
import com.studybuddy.core.domain.usecase.poem.SaveReadingSessionUseCase
import com.studybuddy.core.domain.usecase.poem.ToggleFavouriteUseCase
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.feature.poems.detail.PoemScore
import com.studybuddy.feature.poems.detail.PoemScorer
import com.studybuddy.feature.poems.detail.WordInfo
import com.studybuddy.feature.poems.detail.WordState
import com.studybuddy.shared.whisper.AudioRecorder
import com.studybuddy.shared.whisper.ModelDownloadManager
import com.studybuddy.shared.whisper.WhisperEngine
import com.studybuddy.shared.whisper.WhisperModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * Recording workflow state machine.
 */
enum class RecordingState {
    /** Default state — ready to record. */
    IDLE,

    /** Microphone is active, capturing audio. */
    RECORDING,

    /** Audio captured, running whisper transcription. */
    PROCESSING,

    /** Transcription and scoring complete, results available. */
    SCORED,
}

data class PoemDetailState(
    val poem: Poem? = null,
    val isLoading: Boolean = true,
    val currentReadLine: Int = -1,
    val isReadingAloud: Boolean = false,
    val readingScore: Float? = null,
    // Recording & scoring
    val recordingState: RecordingState = RecordingState.IDLE,
    val words: List<WordInfo> = emptyList(),
    val showResultSheet: Boolean = false,
    val score: PoemScore? = null,
    val currentAmplitude: Float = 0f,
    val isModelLoaded: Boolean = false,
    val hasAudioPermission: Boolean = false,
    // Model download
    val showModelDownload: Boolean = false,
    val modelDownloadProgress: Float = 0f,
)

sealed interface PoemDetailIntent {
    data object ToggleFavourite : PoemDetailIntent
    data object StartReadAloud : PoemDetailIntent
    data object StopReadAloud : PoemDetailIntent
    data class AdvanceReadLine(val lineIndex: Int) : PoemDetailIntent
    data class FinishReading(val score: Float, val durationSeconds: Int) : PoemDetailIntent

    // Recording intents
    data object StartRecording : PoemDetailIntent
    data object StopRecording : PoemDetailIntent
    data class TapWord(val globalIndex: Int) : PoemDetailIntent
    data object DismissResultSheet : PoemDetailIntent
    data class AudioPermissionResult(val granted: Boolean) : PoemDetailIntent
    data object Reset : PoemDetailIntent
}

sealed interface PoemDetailEffect {
    data class SpeakLine(val text: String, val language: String) : PoemDetailEffect
    data object StopSpeaking : PoemDetailEffect
    data class SpeakWord(val text: String, val language: String) : PoemDetailEffect
    data object RequestAudioPermission : PoemDetailEffect
    data class ShowSnackbar(@StringRes val messageResId: Int) : PoemDetailEffect
}

@HiltViewModel
class PoemDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPoemByIdUseCase: GetPoemByIdUseCase,
    private val toggleFavouriteUseCase: ToggleFavouriteUseCase,
    private val saveReadingSessionUseCase: SaveReadingSessionUseCase,
    private val whisperEngine: WhisperEngine,
    private val audioRecorder: AudioRecorder,
    private val modelDownloadManager: ModelDownloadManager,
) : ViewModel() {

    private val poemId: String = checkNotNull(savedStateHandle["poemId"])
    private val profileId = AppConstants.DEFAULT_PROFILE_ID

    private val _state = MutableStateFlow(PoemDetailState())
    val state: StateFlow<PoemDetailState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<PoemDetailEffect>()
    val effects: SharedFlow<PoemDetailEffect> = _effects.asSharedFlow()

    private var recordingJob: Job? = null
    private var amplitudeJob: Job? = null

    init {
        loadPoem()
        checkModelAvailability()
    }

    fun onIntent(intent: PoemDetailIntent) {
        when (intent) {
            is PoemDetailIntent.ToggleFavourite -> toggleFavourite()
            is PoemDetailIntent.StartReadAloud -> startReadAloud()
            is PoemDetailIntent.StopReadAloud -> stopReadAloud()
            is PoemDetailIntent.AdvanceReadLine -> advanceReadLine(intent.lineIndex)
            is PoemDetailIntent.FinishReading -> finishReading(intent.score, intent.durationSeconds)
            is PoemDetailIntent.StartRecording -> startRecording()
            is PoemDetailIntent.StopRecording -> stopRecording()
            is PoemDetailIntent.TapWord -> tapWord(intent.globalIndex)
            is PoemDetailIntent.DismissResultSheet -> dismissResultSheet()
            is PoemDetailIntent.AudioPermissionResult -> onPermissionResult(intent.granted)
            is PoemDetailIntent.Reset -> resetRecording()
        }
    }

    private fun loadPoem() {
        viewModelScope.launch {
            val poem = getPoemByIdUseCase(poemId)
            val words = poem?.let { flattenToWords(it) } ?: emptyList()
            _state.update { it.copy(poem = poem, isLoading = false, words = words) }
        }
    }

    private fun flattenToWords(poem: Poem): List<WordInfo> {
        val words = mutableListOf<WordInfo>()
        var globalIdx = 0
        poem.lines.forEachIndexed { lineIdx, line ->
            line.split(WHITESPACE_REGEX).filter { it.isNotBlank() }.forEachIndexed { wordIdx, word ->
                words.add(
                    WordInfo(
                        text = word,
                        lineIndex = lineIdx,
                        wordIndex = wordIdx,
                        globalIndex = globalIdx,
                    ),
                )
                globalIdx++
            }
        }
        return words
    }

    private fun checkModelAvailability() {
        val modelPath = modelDownloadManager.getModelPath(WhisperModel.TINY)
        if (modelPath != null) {
            viewModelScope.launch {
                whisperEngine.initialize(modelPath).onSuccess {
                    _state.update { it.copy(isModelLoaded = true) }
                }
            }
        }
    }

    private fun toggleFavourite() {
        val poem = _state.value.poem ?: return
        viewModelScope.launch {
            toggleFavouriteUseCase(poem.id, poem.source.name, profileId)
            _state.update {
                it.copy(poem = poem.copy(isFavourite = !poem.isFavourite))
            }
        }
    }

    private fun startReadAloud() {
        val poem = _state.value.poem ?: return
        if (_state.value.recordingState == RecordingState.RECORDING) return
        _state.update { it.copy(isReadingAloud = true, currentReadLine = 0) }
        viewModelScope.launch {
            _effects.emit(PoemDetailEffect.SpeakLine(poem.lines.first(), poem.language))
        }
    }

    private fun stopReadAloud() {
        _state.update { it.copy(isReadingAloud = false, currentReadLine = -1) }
        viewModelScope.launch {
            _effects.emit(PoemDetailEffect.StopSpeaking)
        }
    }

    private fun advanceReadLine(lineIndex: Int) {
        val poem = _state.value.poem ?: return
        val nextLine = lineIndex + 1
        if (nextLine < poem.lines.size) {
            _state.update { it.copy(currentReadLine = nextLine) }
            viewModelScope.launch {
                _effects.emit(PoemDetailEffect.SpeakLine(poem.lines[nextLine], poem.language))
            }
        } else {
            _state.update { it.copy(isReadingAloud = false, currentReadLine = -1) }
        }
    }

    private fun finishReading(
        score: Float,
        durationSeconds: Int,
    ) {
        val poem = _state.value.poem ?: return
        _state.update { it.copy(readingScore = score, isReadingAloud = false) }
        viewModelScope.launch {
            val session = ReadingSession(
                id = UUID.randomUUID().toString(),
                profileId = profileId,
                poemId = poem.id,
                score = score,
                accuracyPct = score * 100f,
                durationSeconds = durationSeconds,
                language = poem.language,
                createdAt = Clock.System.now(),
            )
            saveReadingSessionUseCase(session)
        }
    }

    private fun startRecording() {
        if (_state.value.isReadingAloud) {
            stopReadAloud()
        }

        if (!_state.value.isModelLoaded) {
            // Need to download model first
            downloadAndInitModel()
            return
        }

        if (!_state.value.hasAudioPermission) {
            viewModelScope.launch {
                _effects.emit(PoemDetailEffect.RequestAudioPermission)
            }
            return
        }

        beginRecording()
    }

    private fun downloadAndInitModel() {
        _state.update { it.copy(showModelDownload = true, modelDownloadProgress = 0f) }
        viewModelScope.launch {
            modelDownloadManager.downloadModel(WhisperModel.TINY) { progress ->
                _state.update { it.copy(modelDownloadProgress = progress) }
            }.onSuccess { path ->
                whisperEngine.initialize(path).onSuccess {
                    _state.update { it.copy(isModelLoaded = true, showModelDownload = false) }
                }.onFailure {
                    _state.update { it.copy(showModelDownload = false) }
                    _effects.emit(PoemDetailEffect.ShowSnackbar(CoreUiR.string.poems_model_download_failed))
                }
            }.onFailure {
                _state.update { it.copy(showModelDownload = false) }
                _effects.emit(PoemDetailEffect.ShowSnackbar(CoreUiR.string.poems_model_download_failed))
            }
        }
    }

    @Suppress("MissingPermission")
    private fun beginRecording() {
        val poem = _state.value.poem ?: return

        // Reset words to UNREAD
        val resetWords = _state.value.words.map { it.copy(state = WordState.UNREAD) }
        _state.update {
            it.copy(
                recordingState = RecordingState.RECORDING,
                words = resetWords,
                score = null,
                showResultSheet = false,
                readingScore = null,
            )
        }

        recordingJob = viewModelScope.launch {
            audioRecorder.startRecording()
        }

        amplitudeJob = viewModelScope.launch {
            while (isActive && _state.value.recordingState == RecordingState.RECORDING) {
                val amp = audioRecorder.getCurrentAmplitude()
                _state.update { it.copy(currentAmplitude = amp) }
                delay(AMPLITUDE_UPDATE_MS)
            }
        }
    }

    private fun stopRecording() {
        if (_state.value.recordingState != RecordingState.RECORDING) return

        val poem = _state.value.poem ?: return

        recordingJob?.cancel()
        amplitudeJob?.cancel()
        val samples = audioRecorder.stopRecording()

        _state.update { it.copy(recordingState = RecordingState.PROCESSING, currentAmplitude = 0f) }

        viewModelScope.launch {
            // Join poem lines as initial_prompt to bias the decoder
            val prompt = poem.lines.joinToString(" ")

            val result = whisperEngine.transcribe(
                samples = samples,
                language = poem.language,
                initialPrompt = prompt,
            )

            result.onSuccess { transcription ->
                val poemScore = PoemScorer.score(_state.value.words, transcription.text)
                _state.update {
                    it.copy(
                        recordingState = RecordingState.SCORED,
                        words = poemScore.scoredWords,
                        score = poemScore,
                        showResultSheet = true,
                        readingScore = poemScore.overallAccuracy,
                    )
                }

                // Save reading session
                val session = ReadingSession(
                    id = UUID.randomUUID().toString(),
                    profileId = profileId,
                    poemId = poem.id,
                    score = poemScore.overallAccuracy,
                    accuracyPct = poemScore.overallAccuracy * 100f,
                    durationSeconds = (samples.size / AudioRecorder.SAMPLE_RATE),
                    language = poem.language,
                    createdAt = Clock.System.now(),
                )
                saveReadingSessionUseCase(session)
            }.onFailure {
                _state.update { it.copy(recordingState = RecordingState.IDLE) }
                _effects.emit(PoemDetailEffect.ShowSnackbar(CoreUiR.string.poems_processing_failed))
            }
        }
    }

    private fun tapWord(globalIndex: Int) {
        if (_state.value.recordingState == RecordingState.RECORDING) {
            viewModelScope.launch {
                _effects.emit(PoemDetailEffect.ShowSnackbar(CoreUiR.string.poems_mic_turn_off_first))
            }
            return
        }

        val word = _state.value.words.getOrNull(globalIndex) ?: return
        val poem = _state.value.poem ?: return
        viewModelScope.launch {
            _effects.emit(PoemDetailEffect.SpeakWord(word.text, poem.language))
        }
    }

    private fun dismissResultSheet() {
        _state.update { it.copy(showResultSheet = false) }
    }

    private fun onPermissionResult(granted: Boolean) {
        _state.update { it.copy(hasAudioPermission = granted) }
        if (granted && _state.value.isModelLoaded) {
            beginRecording()
        } else if (!granted) {
            viewModelScope.launch {
                _effects.emit(PoemDetailEffect.ShowSnackbar(CoreUiR.string.poems_mic_permission_rationale))
            }
        }
    }

    private fun resetRecording() {
        recordingJob?.cancel()
        amplitudeJob?.cancel()
        audioRecorder.release()
        val resetWords = _state.value.words.map { it.copy(state = WordState.UNREAD) }
        _state.update {
            it.copy(
                recordingState = RecordingState.IDLE,
                words = resetWords,
                score = null,
                showResultSheet = false,
                currentAmplitude = 0f,
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        recordingJob?.cancel()
        amplitudeJob?.cancel()
        audioRecorder.release()
        whisperEngine.release()
    }

    companion object {
        private val WHITESPACE_REGEX = Regex("\\s+")
        private const val AMPLITUDE_UPDATE_MS = 100L
    }
}
