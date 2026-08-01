package com.studybuddy.feature.math.tables.drill

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.common.constants.PointValues
import com.studybuddy.core.domain.model.PointSource
import com.studybuddy.core.domain.model.mathfacts.MathFact
import com.studybuddy.core.domain.model.mathfacts.TablesCard
import com.studybuddy.core.domain.repository.MathFactsReviewRepository
import com.studybuddy.core.domain.usecase.mathfacts.BuildTablesSessionUseCase
import com.studybuddy.core.domain.usecase.mathfacts.CheckTablesAnswerUseCase
import com.studybuddy.core.domain.usecase.mathfacts.TablesMode
import com.studybuddy.core.domain.usecase.mathfacts.TablesVerdict
import com.studybuddy.core.domain.usecase.points.AwardPointsUseCase
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

enum class TablesDrillPhase { LOADING, DRILLING, RESULTS }

/**
 * The gentle ladder. A wrong answer never scolds: it offers another go, then
 * a strategy, then the answer itself to copy once.
 */
sealed interface TablesFeedback {
    data object Idle : TablesFeedback

    /** Attempt 1: "Presque !" — a free retry with no help yet. */
    data object Nudge : TablesFeedback

    /**
     * Attempt 2: the neighbouring fact, the way CE2 teaches it —
     * "7 × 7 = 49… alors 7 × 8 ?" (add one more row).
     */
    data class Strategy(
        val neighbor: MathFact,
        val neighborProduct: Int,
    ) : TablesFeedback

    /** Attempt 3: reveal the product and let the child type it once. */
    data class Copy(val answer: Int) : TablesFeedback

    data class Correct(
        val pointsEarned: Int,
        val praiseSeed: Int,
    ) : TablesFeedback
}

/** One fact that climbed a Leitner box this session. */
data class TablesGrowth(
    val fact: MathFact,
    val fromBox: Int,
    val toBox: Int,
)

data class TablesDrillState(
    val phase: TablesDrillPhase = TablesDrillPhase.LOADING,
    val cards: List<TablesCard> = emptyList(),
    val index: Int = 0,
    val input: String = "",
    val feedback: TablesFeedback = TablesFeedback.Idle,
    val wrongAttempts: Int = 0,
    val combo: Int = 0,
    val comboPaused: Boolean = false,
    val sessionPoints: Int = 0,
    val firstTryCount: Int = 0,
    val resolvedCount: Int = 0,
    val growths: List<TablesGrowth> = emptyList(),
) {
    val currentCard: TablesCard? get() = cards.getOrNull(index)
    val total: Int get() = cards.size
    val isCopyMode: Boolean get() = feedback is TablesFeedback.Copy
    val isResolved: Boolean get() = feedback is TablesFeedback.Correct
}

sealed interface TablesDrillIntent {
    /** A keypad digit. The custom pad means no IME and no stray characters. */
    data class Digit(val digit: Int) : TablesDrillIntent
    data object Backspace : TablesDrillIntent
    data object Submit : TablesDrillIntent
    data object Replay : TablesDrillIntent
    data object Next : TablesDrillIntent
    data object PlayAgain : TablesDrillIntent
}

@HiltViewModel
class TablesDrillViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val buildSession: BuildTablesSessionUseCase,
    private val checkAnswer: CheckTablesAnswerUseCase,
    private val reviewRepository: MathFactsReviewRepository,
    private val awardPointsUseCase: AwardPointsUseCase,
    private val ttsManager: TtsManager,
) : ViewModel() {

    private val mode: TablesMode = TablesMode.valueOf(checkNotNull(savedStateHandle["mode"]))
    private val table: Int? = savedStateHandle.get<String>("table")?.toIntOrNull()

    private val _state = MutableStateFlow(TablesDrillState())
    val state: StateFlow<TablesDrillState> = _state.asStateFlow()

    /** Facts already requeued once, so a card never loops the session twice. */
    private val requeuedFacts = mutableSetOf<MathFact>()

    init {
        loadSession()
    }

    fun onIntent(intent: TablesDrillIntent) {
        when (intent) {
            is TablesDrillIntent.Digit -> appendDigit(intent.digit)
            TablesDrillIntent.Backspace -> _state.update {
                if (it.isResolved) it else it.copy(input = it.input.dropLast(1))
            }

            TablesDrillIntent.Submit -> submit()
            TablesDrillIntent.Replay -> speakCurrent()
            TablesDrillIntent.Next -> next()
            TablesDrillIntent.PlayAgain -> {
                requeuedFacts.clear()
                _state.value = TablesDrillState()
                loadSession()
            }
        }
    }

    private fun appendDigit(digit: Int) {
        _state.update {
            // Products top out at 90, so three digits is already generous.
            if (it.isResolved || it.input.length >= MAX_INPUT_LENGTH) it else it.copy(input = it.input + digit)
        }
    }

    private fun loadSession() {
        viewModelScope.launch {
            val cards = buildSession(
                mode = mode,
                profileId = AppConstants.DEFAULT_PROFILE_ID,
                now = Clock.System.now(),
                table = table,
            )
            _state.update { it.copy(phase = TablesDrillPhase.DRILLING, cards = cards) }
            speakCurrent()
        }
    }

    private fun submit() {
        val current = _state.value
        val card = current.currentCard ?: return
        if (current.isResolved || current.input.isBlank()) return

        val result = checkAnswer(current.input, card.fact)
        when (result.verdict) {
            TablesVerdict.CORRECT -> resolve(card)
            TablesVerdict.WRONG ->
                if (current.isCopyMode) {
                    // Still copying: keep the answer on screen, never penalise again.
                    _state.update { it.copy(input = "", feedback = TablesFeedback.Copy(result.expected)) }
                } else {
                    climbLadder(card.fact, result.expected)
                }
        }
    }

    private fun climbLadder(
        fact: MathFact,
        expected: Int,
    ) {
        val attempts = _state.value.wrongAttempts + 1
        val neighbor = fact.hintNeighbor
        val feedback = when {
            attempts == 1 -> TablesFeedback.Nudge
            // ×1 facts have no smaller neighbour, so they skip straight to the reveal.
            attempts == 2 && neighbor != null -> {
                speakStrategy(neighbor, fact)
                TablesFeedback.Strategy(neighbor = neighbor, neighborProduct = neighbor.product)
            }

            else -> TablesFeedback.Copy(expected)
        }
        _state.update {
            it.copy(
                wrongAttempts = attempts,
                feedback = feedback,
                // Clear the wrong number so the child starts the next try fresh.
                input = "",
            )
        }
    }

    private fun resolve(card: TablesCard) {
        val current = _state.value
        val firstTry = current.wrongAttempts == 0
        val viaCopy = current.isCopyMode
        val points = when {
            firstTry -> PointValues.MATH_FACTS_FIRST_TRY
            viaCopy -> PointValues.MATH_FACTS_COPY
            else -> PointValues.MATH_FACTS_RETRY
        }

        speakAnswer(card.fact)
        _state.update {
            it.copy(
                feedback = TablesFeedback.Correct(pointsEarned = points, praiseSeed = it.resolvedCount),
                input = card.fact.product.toString(),
                sessionPoints = it.sessionPoints + points,
                firstTryCount = it.firstTryCount + if (firstTry) 1 else 0,
                resolvedCount = it.resolvedCount + 1,
                combo = if (firstTry) it.combo + 1 else it.combo,
                comboPaused = !firstTry,
            )
        }

        // A stumbled fact quietly returns near the end, so the last thing the
        // child does with it is answer it unaided.
        if (!firstTry && requeuedFacts.add(card.fact)) {
            _state.update { it.copy(cards = it.cards + card) }
        }

        viewModelScope.launch {
            val outcome = reviewRepository.recordAnswer(
                profileId = AppConstants.DEFAULT_PROFILE_ID,
                table = card.fact.table,
                multiplicand = card.fact.multiplicand,
                correct = firstTry,
                now = Clock.System.now(),
            )
            val fromBox = outcome.previousBox ?: 0
            if (outcome.review.box > fromBox) {
                _state.update {
                    it.copy(
                        growths = it.growths + TablesGrowth(
                            fact = card.fact,
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
                source = PointSource.MATH,
                reason = "Tables drill: ${card.fact.prompt}",
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
                feedback = TablesFeedback.Idle,
                wrongAttempts = 0,
            )
        }
        speakCurrent()
    }

    private fun finish() {
        _state.update {
            it.copy(
                phase = TablesDrillPhase.RESULTS,
                sessionPoints = it.sessionPoints + PointValues.MATH_FACTS_SESSION_COMPLETE,
            )
        }
        viewModelScope.launch {
            awardPointsUseCase(
                profileId = AppConstants.DEFAULT_PROFILE_ID,
                basePoints = PointValues.MATH_FACTS_SESSION_COMPLETE,
                streak = 0,
                source = PointSource.MATH,
                reason = "Tables drill session complete (${mode.name})",
            )
        }
    }

    /** « sept fois huit ? » — tables are recited aloud at school. */
    private fun speakCurrent() {
        val fact = _state.value.currentCard?.fact ?: return
        ttsManager.speak("${fact.spokenPrompt} ?", Locale.FRENCH)
    }

    /** « sept fois sept, quarante-neuf… alors sept fois huit ? » */
    private fun speakStrategy(
        neighbor: MathFact,
        fact: MathFact,
    ) {
        ttsManager.speak(
            "${neighbor.spokenPrompt}, ${neighbor.product}… alors ${fact.spokenPrompt} ?",
            Locale.FRENCH,
            speed = SLOW_SPEECH_RATE,
        )
    }

    private fun speakAnswer(fact: MathFact) {
        ttsManager.speak("${fact.spokenPrompt}, ${fact.product}", Locale.FRENCH)
    }

    override fun onCleared() {
        ttsManager.stop()
        super.onCleared()
    }

    private companion object {
        const val SLOW_SPEECH_RATE = 0.7f
        const val MAX_INPUT_LENGTH = 3
    }
}
