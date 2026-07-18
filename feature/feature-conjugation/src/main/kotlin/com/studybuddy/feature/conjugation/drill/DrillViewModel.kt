package com.studybuddy.feature.conjugation.drill

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.digitalink.Ink
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.common.constants.PointValues
import com.studybuddy.core.domain.model.PointSource
import com.studybuddy.core.domain.model.conjugation.AtelierCard
import com.studybuddy.core.domain.model.conjugation.ConjugationTense
import com.studybuddy.core.domain.repository.AtelierReviewRepository
import com.studybuddy.core.domain.usecase.conjugation.BuildDrillSessionUseCase
import com.studybuddy.core.domain.usecase.conjugation.CheckDrillAnswerUseCase
import com.studybuddy.core.domain.usecase.conjugation.DrillMode
import com.studybuddy.core.domain.usecase.conjugation.DrillVerdict
import com.studybuddy.core.domain.usecase.points.AwardPointsUseCase
import com.studybuddy.shared.ink.InkRecognitionManager
import com.studybuddy.shared.tts.TtsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

enum class DrillPhase { LOADING, DRILLING, RESULTS }

enum class DrillInputMode { KEYBOARD, STYLUS }

/**
 * The feedback ladder. Near-misses and hearing slips are free retries that
 * never climb it; only a truly wrong form moves Locate → Hint → Copy.
 */
sealed interface DrillFeedback {
    data object Idle : DrillFeedback

    /** Right letters, wrong accents — glow, free retry, full points. */
    data object AccentGlow : DrillFeedback

    /** Apostrophe/spacing slip — glow, free retry, full points. */
    data object ElisionGlow : DrillFeedback

    /** Right form, wrong person: a hearing slip; audio replays, free retry. */
    data object ListenAgain : DrillFeedback

    /** Attempt 1: show where it went wrong, not what is right. */
    data class Locate(val matchedPrefixLength: Int) : DrillFeedback

    /** Attempt 2: the skeleton with the tricky part blanked, plus a slow replay. */
    data class Hint(val skeleton: String) : DrillFeedback

    /** Attempt 3: reveal — write it once to plant it. */
    data class Copy(val correct: String) : DrillFeedback

    data class Correct(
        val pointsEarned: Int,
        val twin: String?,
        val praiseSeed: Int,
    ) : DrillFeedback
}

/** One garden cell that grew this session (box went up). */
data class DrillGrowth(
    val verbInfinitive: String,
    val tense: ConjugationTense,
    val fromBox: Int,
    val toBox: Int,
)

data class DrillState(
    val phase: DrillPhase = DrillPhase.LOADING,
    val cards: List<AtelierCard> = emptyList(),
    val index: Int = 0,
    val input: String = "",
    val inputMode: DrillInputMode = DrillInputMode.KEYBOARD,
    val isRecognizingInk: Boolean = false,
    val inkFailed: Boolean = false,
    val feedback: DrillFeedback = DrillFeedback.Idle,
    val wrongAttempts: Int = 0,
    val combo: Int = 0,
    val comboPaused: Boolean = false,
    val sessionPoints: Int = 0,
    val firstTryCount: Int = 0,
    val resolvedCount: Int = 0,
    val growths: List<DrillGrowth> = emptyList(),
) {
    val currentCard: AtelierCard? get() = cards.getOrNull(index)
    val total: Int get() = cards.size
    val isCopyMode: Boolean get() = feedback is DrillFeedback.Copy
    val isResolved: Boolean get() = feedback is DrillFeedback.Correct
}

sealed interface DrillIntent {
    data class InputChanged(val value: String) : DrillIntent
    data class SetInputMode(val mode: DrillInputMode) : DrillIntent
    data class RecognizeInk(val ink: Ink) : DrillIntent
    data object Submit : DrillIntent
    data object Replay : DrillIntent
    data object Next : DrillIntent
    data object PlayAgain : DrillIntent
}

@HiltViewModel
class DrillViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val buildSession: BuildDrillSessionUseCase,
    private val checkAnswer: CheckDrillAnswerUseCase,
    private val reviewRepository: AtelierReviewRepository,
    private val awardPointsUseCase: AwardPointsUseCase,
    private val ttsManager: TtsManager,
    private val inkRecognitionManager: InkRecognitionManager,
) : ViewModel() {

    private val mode: DrillMode = DrillMode.valueOf(checkNotNull(savedStateHandle["mode"]))
    private val verbId: String? = savedStateHandle["verbId"]
    private val tense: ConjugationTense? =
        savedStateHandle.get<String>("tense")?.let { ConjugationTense.valueOf(it) }

    private val _state = MutableStateFlow(DrillState())
    val state: StateFlow<DrillState> = _state.asStateFlow()

    /** Cards already requeued once — a card never loops the session twice. */
    private val requeuedKeys = mutableSetOf<Triple<String, ConjugationTense, String>>()

    init {
        loadSession()
    }

    fun onIntent(intent: DrillIntent) {
        when (intent) {
            is DrillIntent.InputChanged -> _state.update {
                it.copy(input = intent.value, inkFailed = false)
            }

            is DrillIntent.SetInputMode -> _state.update {
                it.copy(inputMode = intent.mode, input = "", inkFailed = false)
            }

            is DrillIntent.RecognizeInk -> recognizeInk(intent.ink)
            DrillIntent.Submit -> submit()
            DrillIntent.Replay -> speakCurrent()
            DrillIntent.Next -> next()
            DrillIntent.PlayAgain -> {
                requeuedKeys.clear()
                _state.value = DrillState(inputMode = _state.value.inputMode)
                loadSession()
            }
        }
    }

    private fun loadSession() {
        viewModelScope.launch {
            val cards = buildSession(
                mode = mode,
                profileId = AppConstants.DEFAULT_PROFILE_ID,
                now = Clock.System.now(),
                verbId = verbId,
                tense = tense,
            )
            _state.update { it.copy(phase = DrillPhase.DRILLING, cards = cards) }
            speakCurrent()
        }
    }

    private fun submit() {
        val current = _state.value
        val card = current.currentCard ?: return
        if (current.isResolved || current.input.isBlank()) return

        if (current.isCopyMode) {
            submitCopy(card)
            return
        }

        val result = checkAnswer(current.input, card)
        when (result.verdict) {
            DrillVerdict.CORRECT -> resolve(card, twin = null)
            DrillVerdict.CORRECT_TWIN -> resolve(card, twin = result.twin)
            DrillVerdict.ACCENT_MISS -> _state.update { it.copy(feedback = DrillFeedback.AccentGlow) }
            DrillVerdict.ELISION_MISS -> _state.update { it.copy(feedback = DrillFeedback.ElisionGlow) }
            DrillVerdict.WRONG_PRONOUN -> {
                _state.update { it.copy(feedback = DrillFeedback.ListenAgain) }
                speakCurrent()
            }

            DrillVerdict.WRONG -> climbLadder(result.expected, result.matchedPrefixLength)
        }
    }

    private fun climbLadder(
        expected: String,
        matchedPrefixLength: Int,
    ) {
        val attempts = _state.value.wrongAttempts + 1
        val feedback = when {
            attempts == 1 -> DrillFeedback.Locate(matchedPrefixLength)
            attempts == 2 -> {
                ttsManager.speak(expected, Locale.FRENCH, speed = SLOW_SPEECH_RATE)
                DrillFeedback.Hint(skeleton(expected, matchedPrefixLength))
            }

            else -> DrillFeedback.Copy(expected)
        }
        _state.update {
            it.copy(
                wrongAttempts = attempts,
                feedback = feedback,
                // The reveal asks the child to write it fresh.
                input = if (feedback is DrillFeedback.Copy) "" else it.input,
            )
        }
    }

    private fun submitCopy(card: AtelierCard) {
        val verdict = checkAnswer(_state.value.input, card).verdict
        when (verdict) {
            DrillVerdict.CORRECT -> resolve(card, twin = null)
            DrillVerdict.ACCENT_MISS -> _state.update { it.copy(feedback = DrillFeedback.AccentGlow) }
            DrillVerdict.ELISION_MISS -> _state.update { it.copy(feedback = DrillFeedback.ElisionGlow) }
            // Still copying: keep the model answer on screen, no further penalty.
            else -> _state.update { it.copy(feedback = DrillFeedback.Copy(card.prompt)) }
        }
    }

    private fun resolve(
        card: AtelierCard,
        twin: String?,
    ) {
        val current = _state.value
        val firstTry = current.wrongAttempts == 0
        val viaCopy = current.isCopyMode
        val base = when {
            firstTry -> PointValues.CONJUGATION_DRILL_FIRST_TRY
            viaCopy -> PointValues.CONJUGATION_DRILL_COPY
            else -> PointValues.CONJUGATION_DRILL_RETRY
        }
        val stylusBonus = if (current.inputMode == DrillInputMode.STYLUS && !viaCopy) {
            PointValues.CONJUGATION_DRILL_STYLUS_BONUS
        } else {
            0
        }
        val points = base + stylusBonus

        ttsManager.speak(card.prompt, Locale.FRENCH)
        _state.update {
            it.copy(
                feedback = DrillFeedback.Correct(
                    pointsEarned = points,
                    twin = twin,
                    praiseSeed = it.resolvedCount,
                ),
                input = card.prompt,
                sessionPoints = it.sessionPoints + points,
                firstTryCount = it.firstTryCount + if (firstTry) 1 else 0,
                resolvedCount = it.resolvedCount + 1,
                combo = if (firstTry) it.combo + 1 else it.combo,
                comboPaused = !firstTry,
            )
        }

        // A stumbled card silently returns near the end of the session, so the
        // last thing the child does with it is write it right, unaided.
        if (!firstTry) {
            val key = Triple(card.verb.id, card.tense, card.person.name)
            if (requeuedKeys.add(key)) {
                _state.update { it.copy(cards = it.cards + card) }
            }
        }

        viewModelScope.launch {
            val outcome = reviewRepository.recordAnswer(
                profileId = AppConstants.DEFAULT_PROFILE_ID,
                verbId = card.verb.id,
                tense = card.tense,
                person = card.person,
                correct = firstTry,
                now = Clock.System.now(),
            )
            val fromBox = outcome.previousBox ?: 0
            if (outcome.review.box > fromBox) {
                _state.update {
                    it.copy(
                        growths = it.growths + DrillGrowth(
                            verbInfinitive = card.verb.infinitive,
                            tense = card.tense,
                            fromBox = fromBox,
                            toBox = outcome.review.box,
                        ),
                    )
                }
            }
            awardPointsUseCase(
                profileId = AppConstants.DEFAULT_PROFILE_ID,
                basePoints = points,
                streak = 0,
                source = PointSource.CONJUGATION,
                reason = "Atelier drill: ${card.verb.id}/${card.tense.name}/${card.person.name}",
            )
        }
    }

    private fun next() {
        val current = _state.value
        if (!current.isResolved) return
        if (current.index + 1 >= current.cards.size) {
            finish()
            return
        }
        _state.update {
            it.copy(
                index = it.index + 1,
                input = "",
                feedback = DrillFeedback.Idle,
                wrongAttempts = 0,
                inkFailed = false,
            )
        }
        speakCurrent()
    }

    private fun finish() {
        _state.update { it.copy(phase = DrillPhase.RESULTS) }
        viewModelScope.launch {
            awardPointsUseCase(
                profileId = AppConstants.DEFAULT_PROFILE_ID,
                basePoints = PointValues.CONJUGATION_DRILL_SESSION_COMPLETE,
                streak = 0,
                source = PointSource.CONJUGATION,
                reason = "Atelier drill session complete (${mode.name})",
            )
        }
        _state.update {
            it.copy(sessionPoints = it.sessionPoints + PointValues.CONJUGATION_DRILL_SESSION_COMPLETE)
        }
    }

    private fun recognizeInk(ink: Ink) {
        _state.update { it.copy(isRecognizingInk = true, inkFailed = false) }
        viewModelScope.launch {
            val result = inkRecognitionManager.recognize(ink)
            _state.update {
                it.copy(
                    isRecognizingInk = false,
                    input = result.getOrNull()?.lowercase() ?: it.input,
                    inkFailed = result.isFailure,
                )
            }
        }
    }

    private fun speakCurrent() {
        val card = _state.value.currentCard ?: return
        ttsManager.speak(card.prompt, Locale.FRENCH)
    }

    /**
     * The expected answer with everything after the trusted part blanked:
     * pronoun plus one letter always shows, and whatever prefix the child
     * already had right stays visible — "nous allons" → "nous all___".
     */
    private fun skeleton(
        expected: String,
        matchedPrefixLength: Int,
    ): String {
        val pronounEnd = expected.indexOfFirst { it == ' ' || it == '\'' } + 1
        val keep = maxOf(matchedPrefixLength, (pronounEnd + 1).coerceAtMost(expected.length))
        return expected
            .mapIndexed { i, c -> if (i < keep || c == ' ' || c == '\'') c else '_' }
            .joinToString("")
    }

    override fun onCleared() {
        ttsManager.stop()
        super.onCleared()
    }

    companion object {
        const val SLOW_SPEECH_RATE = 0.7f
    }
}
