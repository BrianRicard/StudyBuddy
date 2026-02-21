package com.studybuddy.feature.dictee.practice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.common.constants.PointValues
import com.studybuddy.core.common.locale.SupportedLocale
import com.studybuddy.core.domain.model.DicteeWord
import com.studybuddy.core.domain.model.Feedback
import com.studybuddy.core.domain.model.InputMode
import com.studybuddy.core.domain.model.PointSource
import com.studybuddy.core.domain.repository.DicteeRepository
import com.studybuddy.core.domain.repository.SettingsRepository
import com.studybuddy.core.domain.usecase.dictee.CheckSpellingUseCase
import com.studybuddy.core.domain.usecase.dictee.GetPracticeWordsUseCase
import com.studybuddy.shared.points.AwardPointsUseCase
import com.studybuddy.shared.tts.TtsManager
import com.studybuddy.shared.tts.TtsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DicteePracticeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPracticeWordsUseCase: GetPracticeWordsUseCase,
    private val checkSpellingUseCase: CheckSpellingUseCase,
    private val dicteeRepository: DicteeRepository,
    private val settingsRepository: SettingsRepository,
    private val awardPointsUseCase: AwardPointsUseCase,
    private val ttsManager: TtsManager,
) : ViewModel() {

    private val listId: String = checkNotNull(savedStateHandle["listId"])

    private val _state = MutableStateFlow(DicteePracticeState())
    val state: StateFlow<DicteePracticeState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<DicteePracticeEffect>()
    val effects: SharedFlow<DicteePracticeEffect> = _effects.asSharedFlow()

    private val profileId = AppConstants.DEFAULT_PROFILE_ID
    private var correctInSession = 0
    private var listLanguage = "fr"

    init {
        loadPractice()
        observeTtsState()
    }

    fun onIntent(intent: DicteePracticeIntent) {
        when (intent) {
            is DicteePracticeIntent.PlayWord -> playCurrentWord(TtsManager.SPEED_NORMAL)
            is DicteePracticeIntent.PlayWordSlow -> playCurrentWord(TtsManager.SPEED_SLOW)
            is DicteePracticeIntent.UpdateInput -> {
                _state.update { it.copy(userInput = intent.text) }
            }
            is DicteePracticeIntent.ToggleInputMode -> toggleInputMode()
            is DicteePracticeIntent.ShowHint -> {
                _state.update { it.copy(hintVisible = true) }
            }
            is DicteePracticeIntent.CheckAnswer -> checkAnswer()
            is DicteePracticeIntent.NextWord -> nextWord()
            is DicteePracticeIntent.RetryWord -> retryWord()
            is DicteePracticeIntent.HandwritingRecognized -> {
                _state.update { it.copy(recognizedText = intent.text, userInput = intent.text) }
            }
        }
    }

    private fun loadPractice() {
        viewModelScope.launch {
            val list = dicteeRepository.getList(listId).first() ?: return@launch
            listLanguage = list.language
            _state.update { it.copy(listTitle = list.title) }

            val words = getPracticeWordsUseCase(listId).first()
            _state.update { it.copy(words = words) }

            if (words.isNotEmpty()) {
                playCurrentWord(TtsManager.SPEED_NORMAL)
            }
        }
    }

    private fun observeTtsState() {
        viewModelScope.launch {
            ttsManager.state.collect { ttsState ->
                _state.update { it.copy(isPlaying = ttsState is TtsState.Speaking) }
            }
        }
    }

    private fun playCurrentWord(speed: Float) {
        val word = _state.value.currentWord ?: return
        val locale = SupportedLocale.fromCode(listLanguage).javaLocale
        ttsManager.speak(word.word, locale, speed)
    }

    private fun toggleInputMode() {
        _state.update { currentState ->
            val newMode = when (currentState.inputMode) {
                InputMode.KEYBOARD -> InputMode.HANDWRITING
                InputMode.HANDWRITING -> InputMode.KEYBOARD
            }
            currentState.copy(inputMode = newMode, userInput = "", recognizedText = null)
        }
    }

    private fun checkAnswer() {
        val currentState = _state.value
        val word = currentState.currentWord ?: return
        val input = currentState.userInput.trim()
        if (input.isBlank()) return

        viewModelScope.launch {
            val accentStrict = settingsRepository.isAccentStrict().first()
            val feedback = checkSpellingUseCase(input, word.word, accentStrict)

            when (feedback) {
                is Feedback.Correct -> handleCorrect(word)
                is Feedback.Incorrect -> handleIncorrect(word)
                is Feedback.TimeUp -> { /* Not applicable to dictée */ }
            }

            _state.update { it.copy(feedback = feedback) }
        }
    }

    private suspend fun handleCorrect(word: DicteeWord) {
        val currentState = _state.value
        val newStreak = currentState.streak + 1
        correctInSession++

        val basePoints = when (currentState.inputMode) {
            InputMode.KEYBOARD -> PointValues.DICTEE_CORRECT_TYPED
            InputMode.HANDWRITING -> PointValues.DICTEE_CORRECT_HANDWRITTEN
        }

        val awarded = awardPointsUseCase(
            profileId = profileId,
            basePoints = basePoints,
            streak = newStreak,
            source = PointSource.DICTEE,
            reason = "Dictée: ${word.word}",
        )

        // Update word mastery
        val updatedWord = word.copy(
            attempts = word.attempts + 1,
            correctCount = word.correctCount + 1,
            mastered = (word.correctCount + 1).toFloat() / (word.attempts + 1) >= 0.8f,
            lastAttemptAt = kotlinx.datetime.Clock.System.now(),
        )
        dicteeRepository.updateWord(updatedWord)

        _state.update { it.copy(streak = newStreak, sessionScore = it.sessionScore + awarded) }
        _effects.emit(DicteePracticeEffect.ShowPoints(awarded))
    }

    private suspend fun handleIncorrect(word: DicteeWord) {
        val updatedWord = word.copy(
            attempts = word.attempts + 1,
            lastAttemptAt = kotlinx.datetime.Clock.System.now(),
        )
        dicteeRepository.updateWord(updatedWord)
        _state.update { it.copy(streak = 0) }
    }

    private fun nextWord() {
        val currentState = _state.value
        val nextIndex = currentState.currentIndex + 1

        if (nextIndex >= currentState.words.size) {
            viewModelScope.launch {
                // Check for perfect list bonus
                if (correctInSession == currentState.words.size && currentState.words.isNotEmpty()) {
                    val bonusPoints = awardPointsUseCase(
                        profileId = profileId,
                        basePoints = PointValues.DICTEE_PERFECT_LIST,
                        streak = 0,
                        source = PointSource.DICTEE,
                        reason = "Perfect list: ${currentState.listTitle}",
                    )
                    _state.update { it.copy(sessionScore = it.sessionScore + bonusPoints) }
                }
                _state.update { it.copy(currentIndex = nextIndex, feedback = null) }
                _effects.emit(DicteePracticeEffect.NavigateToResults)
            }
        } else {
            _state.update {
                it.copy(
                    currentIndex = nextIndex,
                    userInput = "",
                    feedback = null,
                    hintVisible = false,
                    recognizedText = null,
                )
            }
            playCurrentWord(TtsManager.SPEED_NORMAL)
        }
    }

    private fun retryWord() {
        _state.update { it.copy(userInput = "", feedback = null, recognizedText = null) }
        playCurrentWord(TtsManager.SPEED_NORMAL)
    }
}
