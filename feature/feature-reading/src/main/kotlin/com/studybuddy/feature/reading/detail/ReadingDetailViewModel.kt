package com.studybuddy.feature.reading.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.domain.model.ReadingDictionaryEntry
import com.studybuddy.core.domain.model.ReadingPassage
import com.studybuddy.core.domain.repository.ReadingRepository
import com.studybuddy.shared.tts.TtsManager
import com.studybuddy.shared.tts.TtsState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReadingDetailState(
    val passage: ReadingPassage? = null,
    val isLoading: Boolean = true,
    val isReadingAloud: Boolean = false,
    val currentHighlightSentence: Int = -1,
    val wordPopup: WordPopupData? = null,
    val readingStartTime: Long = 0L,
)

data class WordPopupData(
    val word: String,
    val entry: ReadingDictionaryEntry?,
)

sealed interface ReadingDetailIntent {
    data object StartReadAloud : ReadingDetailIntent
    data object StopReadAloud : ReadingDetailIntent
    data class TapWord(val word: String) : ReadingDetailIntent
    data object DismissWordPopup : ReadingDetailIntent
    data object ReadyForQuestions : ReadingDetailIntent
    data class SpeakWord(val word: String) : ReadingDetailIntent
}

sealed interface ReadingDetailEffect {
    data class NavigateToQuestions(val passageId: String, val readingTimeMs: Long) :
        ReadingDetailEffect
}

@HiltViewModel
class ReadingDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val readingRepository: ReadingRepository,
    private val ttsManager: TtsManager,
) : ViewModel() {

    private val passageId: String = checkNotNull(savedStateHandle["passageId"])

    private val _state = MutableStateFlow(ReadingDetailState())
    val state: StateFlow<ReadingDetailState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<ReadingDetailEffect>()
    val effects: SharedFlow<ReadingDetailEffect> = _effects.asSharedFlow()

    private var dictionary: List<ReadingDictionaryEntry> = emptyList()

    init {
        loadPassage()
    }

    fun onIntent(intent: ReadingDetailIntent) {
        when (intent) {
            is ReadingDetailIntent.StartReadAloud -> startReadAloud()
            is ReadingDetailIntent.StopReadAloud -> stopReadAloud()
            is ReadingDetailIntent.TapWord -> showWordPopup(intent.word)
            is ReadingDetailIntent.DismissWordPopup -> _state.update { it.copy(wordPopup = null) }
            is ReadingDetailIntent.ReadyForQuestions -> navigateToQuestions()
            is ReadingDetailIntent.SpeakWord -> speakWord(intent.word)
        }
    }

    private fun loadPassage() {
        viewModelScope.launch {
            val passage = readingRepository.getPassageById(passageId)
            if (passage != null) {
                dictionary = readingRepository.getDictionaryEntries(passage.language)
                _state.update {
                    it.copy(
                        passage = passage,
                        isLoading = false,
                        readingStartTime = System.currentTimeMillis(),
                    )
                }
            }
        }
    }

    private fun startReadAloud() {
        val passage = _state.value.passage ?: return
        val locale = when (passage.language.uppercase()) {
            "FR" -> java.util.Locale.FRENCH
            "DE" -> java.util.Locale.GERMAN
            else -> java.util.Locale.ENGLISH
        }
        _state.update { it.copy(isReadingAloud = true, currentHighlightSentence = 0) }

        val sentences = splitSentences(passage.passage)
        viewModelScope.launch {
            sentences.forEachIndexed { index, sentence ->
                if (!_state.value.isReadingAloud) return@launch
                _state.update { it.copy(currentHighlightSentence = index) }
                ttsManager.speak(sentence, locale)
                // Wait for TTS to finish speaking
                ttsManager.state.first { it is TtsState.Ready || it is TtsState.Error }
            }
            _state.update { it.copy(isReadingAloud = false, currentHighlightSentence = -1) }
        }
    }

    private fun stopReadAloud() {
        ttsManager.stop()
        _state.update { it.copy(isReadingAloud = false, currentHighlightSentence = -1) }
    }

    private fun showWordPopup(word: String) {
        val cleaned = word.trim().removeSuffix(",").removeSuffix(".").removeSuffix("!")
            .removeSuffix("?").removeSuffix(":").removeSuffix(";")
        val entry = dictionary.firstOrNull {
            it.word.equals(cleaned, ignoreCase = true)
        }
        _state.update {
            it.copy(wordPopup = WordPopupData(word = cleaned, entry = entry))
        }
    }

    private fun speakWord(word: String) {
        val passage = _state.value.passage ?: return
        val locale = when (passage.language.uppercase()) {
            "FR" -> java.util.Locale.FRENCH
            "DE" -> java.util.Locale.GERMAN
            else -> java.util.Locale.ENGLISH
        }
        ttsManager.speak(word, locale)
    }

    private fun navigateToQuestions() {
        val readingTime = System.currentTimeMillis() - _state.value.readingStartTime
        viewModelScope.launch {
            _effects.emit(ReadingDetailEffect.NavigateToQuestions(passageId, readingTime))
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.stop()
    }

    companion object {
        fun splitSentences(text: String): List<String> {
            return text.split(Regex("(?<=[.!?])\\s+"))
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        }
    }
}
