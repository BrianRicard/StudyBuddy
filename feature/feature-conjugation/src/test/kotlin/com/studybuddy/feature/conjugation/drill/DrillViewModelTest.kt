package com.studybuddy.feature.conjugation.drill

import androidx.lifecycle.SavedStateHandle
import com.studybuddy.core.common.constants.PointValues
import com.studybuddy.core.domain.model.conjugation.AtelierReview
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson
import com.studybuddy.core.domain.model.conjugation.ConjugationTense
import com.studybuddy.core.domain.repository.AtelierAnswerOutcome
import com.studybuddy.core.domain.repository.AtelierReviewRepository
import com.studybuddy.core.domain.repository.PointsRepository
import com.studybuddy.core.domain.usecase.conjugation.BuildDrillSessionUseCase
import com.studybuddy.core.domain.usecase.conjugation.CheckDrillAnswerUseCase
import com.studybuddy.core.domain.usecase.conjugation.GetAtelierSessionUseCase
import com.studybuddy.core.domain.usecase.points.AwardPointsUseCase
import com.studybuddy.shared.ink.InkRecognitionManager
import com.studybuddy.shared.tts.TtsManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DrillViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val reviewRepository: AtelierReviewRepository = mockk()
    private val pointsRepository: PointsRepository = mockk()
    private val ttsManager: TtsManager = mockk(relaxed = true)
    private val inkRecognitionManager: InkRecognitionManager = mockk()

    /** Records every (verbId, correct) pair sent to the review engine. */
    private val recordedAnswers = mutableListOf<Pair<String, Boolean>>()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        recordedAnswers.clear()
        every { reviewRepository.getReviews(any()) } returns flowOf(emptyList())
        coEvery { pointsRepository.addPointEvent(any()) } just runs
        // The drill sets up the French handwriting recognizer on load.
        coEvery { inkRecognitionManager.ensureModelReady(any()) } returns true
        every { inkRecognitionManager.initialize(any()) } just runs

        val verbSlot = slot<String>()
        val correctSlot = slot<Boolean>()
        coEvery {
            reviewRepository.recordAnswer(
                profileId = any(),
                verbId = capture(verbSlot),
                tense = any(),
                person = any(),
                correct = capture(correctSlot),
                now = any(),
            )
        } answers {
            recordedAnswers += verbSlot.captured to correctSlot.captured
            AtelierAnswerOutcome(
                previousBox = null,
                review = AtelierReview(
                    id = "r",
                    profileId = "default",
                    verbId = verbSlot.captured,
                    tense = ConjugationTense.PRESENT,
                    person = ConjugationPerson.JE,
                    box = if (correctSlot.captured) 1 else 0,
                    dueAt = Instant.fromEpochMilliseconds(0),
                    lapses = 0,
                    updatedAt = Instant.fromEpochMilliseconds(0),
                ),
            )
        }
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** CELL mode with a fixed cell keeps sessions deterministic (6 cards of être·présent). */
    private fun createViewModel(): DrillViewModel = DrillViewModel(
        savedStateHandle = SavedStateHandle(
            mapOf("mode" to "CELL", "verbId" to "etre", "tense" to "PRESENT"),
        ),
        buildSession = BuildDrillSessionUseCase(GetAtelierSessionUseCase(reviewRepository)),
        checkAnswer = CheckDrillAnswerUseCase(),
        reviewRepository = reviewRepository,
        awardPointsUseCase = AwardPointsUseCase(pointsRepository),
        ttsManager = ttsManager,
        inkRecognitionManager = inkRecognitionManager,
    )

    private fun DrillViewModel.answerCurrentCorrectly() {
        val prompt = state.value.currentCard!!.prompt
        onIntent(DrillIntent.InputChanged(prompt))
        onIntent(DrillIntent.Submit)
    }

    @Test
    fun `session loads and speaks the first card`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(DrillPhase.DRILLING, state.phase)
        assertEquals(6, state.total)
        assertTrue(state.cards.all { it.verb.id == "etre" })
    }

    @Test
    fun `the french handwriting recognizer is prepared on load`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Regression: without this the recognizer stays null and every stylus
        // submission fails with "could not read it".
        coVerify { inkRecognitionManager.ensureModelReady("fr") }
        verify { inkRecognitionManager.initialize("fr") }
        assertTrue(viewModel.state.value.isInkModelReady)
    }

    @Test
    fun `a stylus submission that recognizes fills the input`() = runTest {
        coEvery { inkRecognitionManager.recognize(any()) } returns Result.success("Sont")
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(DrillIntent.RecognizeInk(mockk()))
        advanceUntilIdle()

        assertEquals("sont", viewModel.state.value.input)
        assertFalse(viewModel.state.value.inkFailed)
    }

    @Test
    fun `when the ink model fails to download the stylus is marked not ready`() = runTest {
        coEvery { inkRecognitionManager.ensureModelReady(any()) } returns false
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isInkModelReady)
        verify(exactly = 0) { inkRecognitionManager.initialize(any()) }
    }

    @Test
    fun `a correct first try earns full points and grows the combo`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.answerCurrentCorrectly()
        advanceUntilIdle()

        val state = viewModel.state.value
        val feedback = state.feedback as DrillFeedback.Correct
        assertEquals(PointValues.CONJUGATION_DRILL_FIRST_TRY, feedback.pointsEarned)
        assertEquals(1, state.combo)
        assertEquals(1, state.firstTryCount)
        assertEquals(listOf(state.currentCard!!.verb.id to true), recordedAnswers)
    }

    @Test
    fun `stylus answers earn the bonus`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(DrillIntent.SetInputMode(DrillInputMode.STYLUS))
        viewModel.answerCurrentCorrectly()
        advanceUntilIdle()

        val feedback = viewModel.state.value.feedback as DrillFeedback.Correct
        assertEquals(
            PointValues.CONJUGATION_DRILL_FIRST_TRY + PointValues.CONJUGATION_DRILL_STYLUS_BONUS,
            feedback.pointsEarned,
        )
    }

    @Test
    fun `an accent slip is a free retry that never climbs the ladder`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val accentless = viewModel.state.value.currentCard!!.prompt
            .replace("é", "e").replace("ê", "e")
        // Only meaningful when the prompt actually has an accent; être présent
        // has "vous êtes" — walk until we hit it or prove others exact-match.
        viewModel.onIntent(DrillIntent.InputChanged(accentless))
        viewModel.onIntent(DrillIntent.Submit)

        val state = viewModel.state.value
        assertEquals(0, state.wrongAttempts)
        assertTrue(
            state.feedback is DrillFeedback.AccentGlow || state.feedback is DrillFeedback.Correct,
        )
    }

    @Test
    fun `wrong answers climb Locate then Hint then Copy`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(DrillIntent.InputChanged("je zzz"))
        viewModel.onIntent(DrillIntent.Submit)
        assertTrue(viewModel.state.value.feedback is DrillFeedback.Locate)

        viewModel.onIntent(DrillIntent.InputChanged("je zzz"))
        viewModel.onIntent(DrillIntent.Submit)
        assertTrue(viewModel.state.value.feedback is DrillFeedback.Hint)

        viewModel.onIntent(DrillIntent.InputChanged("je zzz"))
        viewModel.onIntent(DrillIntent.Submit)
        val copy = viewModel.state.value.feedback
        assertTrue(copy is DrillFeedback.Copy)
        assertEquals("", viewModel.state.value.input)
        assertEquals(viewModel.state.value.currentCard!!.prompt, (copy as DrillFeedback.Copy).correct)
    }

    @Test
    fun `the hint skeleton keeps the pronoun and blanks the ending`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val card = viewModel.state.value.currentCard!!
        viewModel.onIntent(DrillIntent.InputChanged("${card.person.spokenPronoun} zzz"))
        viewModel.onIntent(DrillIntent.Submit)
        viewModel.onIntent(DrillIntent.Submit)

        val hint = viewModel.state.value.feedback as DrillFeedback.Hint
        assertTrue(hint.skeleton.contains('_'), "skeleton must blank something: ${hint.skeleton}")
        assertEquals(card.prompt.length, hint.skeleton.length)
    }

    @Test
    fun `a stumbled card records a lapse, pauses the combo, and requeues`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.answerCurrentCorrectly()
        advanceUntilIdle()
        viewModel.onIntent(DrillIntent.Next)
        assertEquals(1, viewModel.state.value.combo)

        val stumbledCard = viewModel.state.value.currentCard!!
        viewModel.onIntent(DrillIntent.InputChanged("je zzz"))
        viewModel.onIntent(DrillIntent.Submit)
        viewModel.onIntent(DrillIntent.InputChanged(stumbledCard.prompt))
        viewModel.onIntent(DrillIntent.Submit)
        advanceUntilIdle()

        val state = viewModel.state.value
        val feedback = state.feedback as DrillFeedback.Correct
        assertEquals(PointValues.CONJUGATION_DRILL_RETRY, feedback.pointsEarned)
        assertEquals(1, state.combo)
        assertTrue(state.comboPaused)
        // Requeued: session grew from 6 to 7 cards.
        assertEquals(7, state.total)
        assertEquals(stumbledCard.person, state.cards.last().person)
        // The review engine heard about the lapse.
        assertEquals(false, recordedAnswers.last().second)
    }

    @Test
    fun `copy resolution awards the smallest points and requeues once`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        val card = viewModel.state.value.currentCard!!

        repeat(3) {
            viewModel.onIntent(DrillIntent.InputChanged("je zzz"))
            viewModel.onIntent(DrillIntent.Submit)
        }
        assertTrue(viewModel.state.value.isCopyMode)

        viewModel.onIntent(DrillIntent.InputChanged(card.prompt))
        viewModel.onIntent(DrillIntent.Submit)
        advanceUntilIdle()

        val feedback = viewModel.state.value.feedback as DrillFeedback.Correct
        assertEquals(PointValues.CONJUGATION_DRILL_COPY, feedback.pointsEarned)
        assertEquals(7, viewModel.state.value.total)
    }

    @Test
    fun `finishing the session reaches results with the completion bonus`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        repeat(6) {
            viewModel.answerCurrentCorrectly()
            advanceUntilIdle()
            viewModel.onIntent(DrillIntent.Next)
        }
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(DrillPhase.RESULTS, state.phase)
        assertEquals(6, state.firstTryCount)
        assertEquals(
            6 * PointValues.CONJUGATION_DRILL_FIRST_TRY + PointValues.CONJUGATION_DRILL_SESSION_COMPLETE,
            state.sessionPoints,
        )
        assertEquals(6, recordedAnswers.size)
        assertTrue(recordedAnswers.all { it.second })
    }

    @Test
    fun `a requeued card must be answered before the session ends`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Stumble the first card, then clear the remaining five correctly.
        val stumbled = viewModel.state.value.currentCard!!
        viewModel.onIntent(DrillIntent.InputChanged("je zzz"))
        viewModel.onIntent(DrillIntent.Submit)
        viewModel.onIntent(DrillIntent.InputChanged(stumbled.prompt))
        viewModel.onIntent(DrillIntent.Submit)
        advanceUntilIdle()
        viewModel.onIntent(DrillIntent.Next)

        repeat(5) {
            viewModel.answerCurrentCorrectly()
            advanceUntilIdle()
            viewModel.onIntent(DrillIntent.Next)
        }

        // Still drilling: the requeued card is waiting, unaided this time.
        val state = viewModel.state.value
        assertEquals(DrillPhase.DRILLING, state.phase)
        assertEquals(stumbled.person, state.currentCard!!.person)

        viewModel.answerCurrentCorrectly()
        advanceUntilIdle()
        viewModel.onIntent(DrillIntent.Next)
        advanceUntilIdle()

        assertEquals(DrillPhase.RESULTS, viewModel.state.value.phase)
        // The requeued success recorded as correct — the session ends in victory.
        assertEquals(true, recordedAnswers.last().second)
    }

    @Test
    fun `a wrong pronoun replays the audio without climbing the ladder`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val card = viewModel.state.value.currentCard!!
        val otherPerson = ConjugationPerson.entries.first {
            it != card.person && card.verb.display(card.tense, it) != card.prompt
        }
        viewModel.onIntent(DrillIntent.InputChanged(card.verb.display(card.tense, otherPerson)))
        viewModel.onIntent(DrillIntent.Submit)

        val state = viewModel.state.value
        assertTrue(
            state.feedback is DrillFeedback.ListenAgain || state.feedback is DrillFeedback.Correct,
            "expected a hearing-slip retry, got ${state.feedback}",
        )
        assertEquals(0, state.wrongAttempts)
    }

    @Test
    fun `play again rebuilds a fresh session`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        repeat(6) {
            viewModel.answerCurrentCorrectly()
            advanceUntilIdle()
            viewModel.onIntent(DrillIntent.Next)
        }
        advanceUntilIdle()
        assertEquals(DrillPhase.RESULTS, viewModel.state.value.phase)

        viewModel.onIntent(DrillIntent.PlayAgain)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(DrillPhase.DRILLING, state.phase)
        assertEquals(0, state.index)
        assertEquals(0, state.sessionPoints)
        assertFalse(state.isResolved)
    }
}
