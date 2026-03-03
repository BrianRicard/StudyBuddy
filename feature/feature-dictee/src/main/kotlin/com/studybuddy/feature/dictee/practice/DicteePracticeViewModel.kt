package com.studybuddy.feature.dictee.practice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.digitalink.Ink
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.common.locale.SupportedLocale
import com.studybuddy.core.domain.model.DicteeWord
import com.studybuddy.core.domain.model.Difficulty
import com.studybuddy.core.domain.model.InputMode
import com.studybuddy.core.domain.model.PointSource
import com.studybuddy.core.domain.repository.DicteeRepository
import com.studybuddy.core.domain.repository.SettingsRepository
import com.studybuddy.core.domain.usecase.dictee.GetMixedPracticeWordsUseCase
import com.studybuddy.core.domain.usecase.dictee.GetPracticeWordsUseCase
import com.studybuddy.shared.ink.InkRecognitionManager
import com.studybuddy.shared.points.AwardPointsUseCase
import com.studybuddy.shared.points.RewardCalculator
import com.studybuddy.shared.points.RewardInput
import com.studybuddy.shared.tts.TtsManager
import com.studybuddy.shared.tts.TtsState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DicteePracticeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPracticeWordsUseCase: GetPracticeWordsUseCase,
    private val getMixedPracticeWordsUseCase: GetMixedPracticeWordsUseCase,
    private val dicteeRepository: DicteeRepository,
    private val settingsRepository: SettingsRepository,
    private val awardPointsUseCase: AwardPointsUseCase,
    private val rewardCalculator: RewardCalculator,
    private val ttsManager: TtsManager,
    private val inkRecognitionManager: InkRecognitionManager,
) : ViewModel() {

    // Single-list mode passes "listId"; challenge mode passes "listIds" (pipe-separated UUIDs)
    private val listId: String? = savedStateHandle["listId"]
    private val listIdsRaw: String? = savedStateHandle["listIds"]
    private val isChallengeMode: Boolean = listIdsRaw != null
    private val allListIds: List<String> = when {
        listIdsRaw != null -> listIdsRaw.split("|").filter { it.isNotBlank() }
        listId != null -> listOf(listId)
        else -> emptyList()
    }

    private val _state = MutableStateFlow(DicteePracticeState(isChallengeMode = isChallengeMode))
    val state: StateFlow<DicteePracticeState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<DicteePracticeEffect>()
    val effects: SharedFlow<DicteePracticeEffect> = _effects.asSharedFlow()

    private val profileId = AppConstants.DEFAULT_PROFILE_ID

    /** Maps listId -> language code for multi-language challenge sessions. */
    private var listLanguageMap: Map<String, String> = emptyMap()

    /** Fallback language for single-list sessions. */
    private var listLanguage = "fr"

    /** Accent strictness setting. */
    private var isAccentStrict = false

    init {
        loadPractice()
        observeTtsState()
    }

    fun onIntent(intent: DicteePracticeIntent) {
        when (intent) {
            is DicteePracticeIntent.PlayWord -> playCurrentWord(TtsManager.SPEED_NORMAL)
            is DicteePracticeIntent.PlayWordSlow -> playCurrentWord(TtsManager.SPEED_SLOW)
            is DicteePracticeIntent.UpdateInput -> {
                _state.update {
                    it.copy(
                        userInput = intent.text,
                        sessionState = when (it.inputMode) {
                            InputMode.HANDWRITING -> DicteeSessionState.HANDWRITING
                            else -> DicteeSessionState.TYPING
                        },
                    )
                }
            }
            is DicteePracticeIntent.SwitchInputMode -> switchInputMode(intent.mode)
            is DicteePracticeIntent.ShowHint -> {
                _state.update { it.copy(hintVisible = true) }
            }
            is DicteePracticeIntent.CheckAnswer -> checkAnswer()
            is DicteePracticeIntent.NextWord -> nextWord()
            is DicteePracticeIntent.RetryWord -> retryWord()
            is DicteePracticeIntent.SkipWord -> skipWord()
            is DicteePracticeIntent.HandwritingRecognized -> {
                _state.update {
                    it.copy(
                        recognizedText = intent.text,
                        userInput = intent.text,
                        sessionState = DicteeSessionState.HANDWRITING,
                    )
                }
            }
            is DicteePracticeIntent.RecognizeInk -> recognizeInk(intent.ink)
            is DicteePracticeIntent.TapTile -> tapTile(intent.tileIndex)
            is DicteePracticeIntent.RemoveFromSlot -> removeFromSlot(intent.slotIndex)
        }
    }

    private fun recognizeInk(ink: Ink) {
        _state.update { it.copy(recognitionPending = true, recognitionErrorResId = null) }
        viewModelScope.launch {
            val result = inkRecognitionManager.recognize(ink)
            result.onSuccess { text ->
                if (text.isNotBlank()) {
                    _state.update { it.copy(recognitionPending = false) }
                    onIntent(DicteePracticeIntent.HandwritingRecognized(text))
                } else {
                    _state.update {
                        it.copy(
                            recognitionPending = false,
                            recognitionErrorResId = com.studybuddy.core.ui.R.string.dictee_recognition_error,
                        )
                    }
                }
            }.onFailure {
                // Recognition failed — canvas still works for practice.
                // Try to download model in the background for next attempt.
                _state.update {
                    it.copy(
                        recognitionPending = false,
                        isInkModelReady = false,
                    )
                }
                ensureInkModelReady(listLanguage)
            }
        }
    }

    private fun ensureInkModelReady(languageTag: String) {
        viewModelScope.launch {
            val ready = inkRecognitionManager.ensureModelReady(languageTag)
            if (ready) {
                inkRecognitionManager.initialize(languageTag)
            }
            _state.update { it.copy(isInkModelReady = ready) }
        }
    }

    private fun loadPractice() {
        viewModelScope.launch {
            isAccentStrict = settingsRepository.isAccentStrict().first()
            if (isChallengeMode) {
                loadChallengeSession()
            } else {
                loadSingleListSession()
            }
        }
    }

    private suspend fun loadSingleListSession() {
        val id = allListIds.firstOrNull() ?: return
        val list = dicteeRepository.getList(id).first() ?: return
        listLanguage = list.language
        inkRecognitionManager.initialize(list.language)
        _state.update { it.copy(listTitle = list.title) }

        val words = getPracticeWordsUseCase(id).first()
        _state.update { it.copy(words = words) }

        // Check model readiness in background — canvas works regardless
        ensureInkModelReady(list.language)
    }

    private suspend fun loadChallengeSession() {
        val lists = allListIds.mapNotNull { dicteeRepository.getList(it).first() }
        listLanguageMap = lists.associate { it.id to it.language }

        val title = when {
            lists.size <= 2 -> lists.joinToString(" + ") { it.title }
            else -> "${lists.take(2).joinToString(" + ") { it.title }} + ${lists.size - 2} more"
        }
        _state.update { it.copy(listTitle = "Challenge: $title") }

        val words = getMixedPracticeWordsUseCase(allListIds).first()
        _state.update { it.copy(words = words) }
    }

    private fun observeTtsState() {
        viewModelScope.launch {
            ttsManager.state.collect { ttsState ->
                val wasPlaying = _state.value.isPlaying
                val isNowPlaying = ttsState is TtsState.Speaking
                _state.update {
                    it.copy(
                        isPlaying = isNowPlaying,
                        sessionState = when {
                            isNowPlaying -> DicteeSessionState.LISTENING
                            wasPlaying && !isNowPlaying &&
                                it.sessionState == DicteeSessionState.LISTENING -> {
                                // Audio finished, return to idle (input now enabled)
                                if (it.feedback != null) {
                                    DicteeSessionState.SCORED
                                } else {
                                    DicteeSessionState.IDLE
                                }
                            }
                            else -> it.sessionState
                        },
                        hasListenedAtLeastOnce = it.hasListenedAtLeastOnce || isNowPlaying,
                    )
                }
            }
        }
    }

    private fun playCurrentWord(speed: Float) {
        val word = _state.value.currentWord ?: return
        val langCode = if (isChallengeMode) {
            listLanguageMap[word.listId] ?: "fr"
        } else {
            listLanguage
        }
        val locale = SupportedLocale.fromCode(langCode).javaLocale
        _state.update {
            it.copy(
                replayCount = it.replayCount + 1,
            )
        }
        ttsManager.speak(word.word, locale, speed)
    }

    private fun switchInputMode(mode: InputMode) {
        _state.update { currentState ->
            val newState = currentState.copy(
                inputMode = mode,
                userInput = "",
                recognizedText = null,
                recognitionErrorResId = null,
                sessionState = if (currentState.hasListenedAtLeastOnce) {
                    DicteeSessionState.IDLE
                } else {
                    currentState.sessionState
                },
            )
            if (mode == InputMode.LETTER_TILES) {
                setupLetterTiles(newState)
            } else {
                newState.copy(letterTiles = emptyList(), answerSlots = emptyList())
            }
        }
        if (mode == InputMode.HANDWRITING && !_state.value.isInkModelReady) {
            ensureInkModelReady(listLanguage)
        }
    }

    private fun setupLetterTiles(state: DicteePracticeState): DicteePracticeState {
        val word = state.currentWord ?: return state
        val letters = word.word.lowercase().toList()
        // Add 2-3 distractor letters
        val distractors = generateDistractors(letters, count = minOf(3, letters.size / 2 + 1))
        val allLetters = (letters + distractors).shuffled()
        val tiles = allLetters.mapIndexed { index, letter ->
            LetterTile(letter = letter, originalIndex = index)
        }
        val slots = List<Char?>(word.word.length) { null }
        return state.copy(letterTiles = tiles, answerSlots = slots)
    }

    private fun generateDistractors(
        wordLetters: List<Char>,
        count: Int,
    ): List<Char> {
        val alphabet = ('a'..'z').toList()
        val available = alphabet - wordLetters.toSet()
        return available.shuffled().take(count)
    }

    private fun tapTile(tileIndex: Int) {
        _state.update { currentState ->
            val tiles = currentState.letterTiles.toMutableList()
            val slots = currentState.answerSlots.toMutableList()
            if (tileIndex >= tiles.size || tiles[tileIndex].isUsed) return@update currentState

            val firstEmptySlot = slots.indexOfFirst { it == null }
            if (firstEmptySlot < 0) return@update currentState

            tiles[tileIndex] = tiles[tileIndex].copy(isUsed = true)
            slots[firstEmptySlot] = tiles[tileIndex].letter

            val userInput = slots.filterNotNull().joinToString("")
            currentState.copy(
                letterTiles = tiles,
                answerSlots = slots,
                userInput = userInput,
                sessionState = DicteeSessionState.TYPING,
            )
        }
    }

    private fun removeFromSlot(slotIndex: Int) {
        _state.update { currentState ->
            val tiles = currentState.letterTiles.toMutableList()
            val slots = currentState.answerSlots.toMutableList()
            val removedChar = slots[slotIndex] ?: return@update currentState

            slots[slotIndex] = null

            // Find and un-use the first matching tile
            val tileIdx = tiles.indexOfFirst { it.isUsed && it.letter == removedChar }
            if (tileIdx >= 0) {
                tiles[tileIdx] = tiles[tileIdx].copy(isUsed = false)
            }

            val userInput = slots.filterNotNull().joinToString("")
            currentState.copy(
                letterTiles = tiles,
                answerSlots = slots,
                userInput = userInput,
            )
        }
    }

    private fun checkAnswer() {
        val currentState = _state.value
        val word = currentState.currentWord ?: return
        val input = currentState.userInput.trim()
        if (input.isBlank()) return

        _state.update { it.copy(sessionState = DicteeSessionState.CHECKING) }

        viewModelScope.launch {
            // Artificial delay to match poems rhythm (the scoring is instant)
            delay(CHECKING_DELAY_MS)

            val score = DicteeScorer.scoreWord(
                referenceWord = word.word,
                childAnswer = input,
                language = getCurrentLanguage(word),
                inputMode = currentState.inputMode,
            )

            if (score.isCorrect) {
                handleCorrect(word, score)
            } else {
                handleIncorrect(word, score)
            }

            _state.update {
                it.copy(
                    feedback = score,
                    sessionState = DicteeSessionState.SCORED,
                    sessionResults = it.sessionResults + score,
                )
            }
        }
    }

    private suspend fun handleCorrect(
        word: DicteeWord,
        score: DicteeWordScore,
    ) {
        val currentState = _state.value
        val newStreak = currentState.streak + 1

        val updatedWord = word.copy(
            attempts = word.attempts + 1,
            correctCount = word.correctCount + 1,
            mastered = (word.correctCount + 1).toFloat() / (word.attempts + 1) >= 0.8f,
            lastAttemptAt = kotlinx.datetime.Clock.System.now(),
        )
        dicteeRepository.updateWord(updatedWord)

        _state.update { it.copy(streak = newStreak) }
    }

    private suspend fun handleIncorrect(
        word: DicteeWord,
        score: DicteeWordScore,
    ) {
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
                // Award points for the full session via RewardCalculator
                val correctCount = currentState.sessionResults.count { it.isCorrect }
                val totalWords = currentState.sessionResults.size
                val avgSimilarity = if (currentState.sessionResults.isNotEmpty()) {
                    currentState.sessionResults.map { it.similarity }.average().toFloat()
                } else {
                    0f
                }

                val reward = rewardCalculator.calculate(
                    RewardInput.DicteeReward(
                        correctWords = correctCount,
                        totalWords = totalWords,
                        inputMode = currentState.inputMode,
                        difficulty = Difficulty.MEDIUM,
                        averageSimilarity = avgSimilarity,
                    ),
                )

                val awarded = awardPointsUseCase(
                    profileId = profileId,
                    basePoints = reward.totalPoints,
                    streak = 0,
                    source = PointSource.DICTEE,
                    reason = "Dictée session: $correctCount/$totalWords correct",
                )

                _state.update {
                    it.copy(
                        currentIndex = nextIndex,
                        feedback = null,
                        sessionState = DicteeSessionState.IDLE,
                        sessionScore = awarded,
                    )
                }
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
                    recognitionErrorResId = null,
                    hasListenedAtLeastOnce = false,
                    replayCount = 0,
                    sessionState = DicteeSessionState.IDLE,
                    letterTiles = emptyList(),
                    answerSlots = emptyList(),
                )
            }
            // Setup letter tiles if that mode is active
            if (_state.value.inputMode == InputMode.LETTER_TILES) {
                _state.update { setupLetterTiles(it) }
            }
        }
    }

    private fun retryWord() {
        _state.update {
            var newState = it.copy(
                userInput = "",
                feedback = null,
                recognizedText = null,
                recognitionErrorResId = null,
                sessionState = DicteeSessionState.IDLE,
                // Remove the last result since we're retrying
                sessionResults = if (it.sessionResults.isNotEmpty()) {
                    it.sessionResults.dropLast(1)
                } else {
                    it.sessionResults
                },
            )
            if (it.inputMode == InputMode.LETTER_TILES) {
                newState = setupLetterTiles(newState)
            }
            newState
        }
        playCurrentWord(TtsManager.SPEED_NORMAL)
    }

    private fun skipWord() {
        val word = _state.value.currentWord ?: return
        val skipScore = DicteeWordScore(
            referenceWord = word.word,
            childAnswer = "",
            scoredLetters = word.word.map { char ->
                ScoredLetter(
                    character = null,
                    referenceCharacter = char,
                    status = LetterStatus.MISSING,
                )
            },
            similarity = 0f,
            starRating = 1,
            isCorrect = false,
            encouragementResId = DicteeScorer.encouragementForStars(1),
            inputMode = _state.value.inputMode,
        )
        _state.update {
            it.copy(
                sessionResults = it.sessionResults + skipScore,
                streak = 0,
            )
        }
        nextWord()
    }

    private fun getCurrentLanguage(word: DicteeWord): String {
        return if (isChallengeMode) {
            listLanguageMap[word.listId] ?: "fr"
        } else {
            listLanguage
        }
    }

    companion object {
        private const val CHECKING_DELAY_MS = 600L
    }
}
