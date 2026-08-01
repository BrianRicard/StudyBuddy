package com.studybuddy.feature.math.tables.drill

import androidx.lifecycle.SavedStateHandle
import com.studybuddy.core.common.constants.PointValues
import com.studybuddy.core.domain.model.mathfacts.MathFact
import com.studybuddy.core.domain.model.mathfacts.MathFactReview
import com.studybuddy.core.domain.repository.MathFactAnswerOutcome
import com.studybuddy.core.domain.repository.MathFactsReviewRepository
import com.studybuddy.core.domain.usecase.mathfacts.BuildTablesSessionUseCase
import com.studybuddy.core.domain.usecase.mathfacts.CheckTablesAnswerUseCase
import com.studybuddy.core.domain.usecase.points.AwardPointsUseCase
import com.studybuddy.shared.tts.TtsManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.Locale
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
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TablesDrillViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository: MathFactsReviewRepository = mockk(relaxed = true)
    private val awardPointsUseCase: AwardPointsUseCase = mockk(relaxed = true)
    private val ttsManager: TtsManager = mockk(relaxed = true)

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.getReviews(any()) } returns flowOf(emptyList())
        coEvery { repository.recordAnswer(any(), any(), any(), any(), any()) } answers {
            val table = arg<Int>(1)
            val multiplicand = arg<Int>(2)
            MathFactAnswerOutcome(
                previousBox = 0,
                review = review(table, multiplicand, box = 1),
            )
        }
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun review(
        table: Int,
        multiplicand: Int,
        box: Int,
    ) = MathFactReview(
        id = "$table-$multiplicand",
        profileId = "default",
        table = table,
        multiplicand = multiplicand,
        box = box,
        dueAt = NOW,
        lapses = 0,
        updatedAt = NOW,
    )

    /**
     * TABLE mode yields all ten facts of the table de 7. The *set* is fixed but
     * the order is shuffled, so tests that care about a particular fact walk to
     * it rather than assuming it comes first.
     */
    private fun createViewModel(
        mode: String = "TABLE",
        table: String? = "7",
    ) = TablesDrillViewModel(
        savedStateHandle = SavedStateHandle(
            buildMap {
                put("mode", mode)
                if (table != null) put("table", table)
            },
        ),
        buildSession = BuildTablesSessionUseCase(repository),
        checkAnswer = CheckTablesAnswerUseCase(),
        reviewRepository = repository,
        awardPointsUseCase = awardPointsUseCase,
        ttsManager = ttsManager,
    )

    private fun TablesDrillViewModel.type(answer: Int) {
        answer.toString().forEach { onIntent(TablesDrillIntent.Digit(it.digitToInt())) }
    }

    private fun TablesDrillViewModel.answerCurrentCorrectly() {
        val fact = state.value.currentCard!!.fact
        type(fact.product)
        onIntent(TablesDrillIntent.Submit)
    }

    /** Walks (answering cleanly) until the current fact has a smaller neighbour. */
    private fun TablesDrillViewModel.driveToFactWithNeighbor(): MathFact = driveUntil { it.multiplicand > 1 }

    /** Walks (answering cleanly) until the current fact is a ×1, which has no neighbour. */
    private fun TablesDrillViewModel.driveToTimesOneFact(): MathFact = driveUntil { it.multiplicand == 1 }

    private fun TablesDrillViewModel.driveUntil(predicate: (MathFact) -> Boolean): MathFact {
        while (!predicate(state.value.currentCard!!.fact)) {
            answerCurrentCorrectly()
            testDispatcher.scheduler.advanceUntilIdle()
            onIntent(TablesDrillIntent.Next)
            testDispatcher.scheduler.advanceUntilIdle()
        }
        return state.value.currentCard!!.fact
    }

    @Test
    fun `a table session loads its ten facts and starts drilling`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(TablesDrillPhase.DRILLING, state.phase)
        assertEquals(10, state.total)
        assertTrue(state.cards.all { it.fact.table == 7 })
    }

    @Test
    fun `the keypad builds the answer and backspace removes a digit`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.type(56)
        assertEquals("56", viewModel.state.value.input)

        viewModel.onIntent(TablesDrillIntent.Backspace)
        assertEquals("5", viewModel.state.value.input)
    }

    @Test
    fun `a first-try answer earns full points and grows the combo`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.answerCurrentCorrectly()
        advanceUntilIdle()

        val state = viewModel.state.value
        val feedback = assertInstanceOf(TablesFeedback.Correct::class.java, state.feedback)
        assertEquals(PointValues.MATH_FACTS_FIRST_TRY, feedback.pointsEarned)
        assertEquals(1, state.firstTryCount)
        assertEquals(1, state.combo)
        assertFalse(state.comboPaused)
    }

    @Test
    fun `the first wrong answer only nudges, it never reveals`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.type(99)
        viewModel.onIntent(TablesDrillIntent.Submit)

        val state = viewModel.state.value
        assertEquals(TablesFeedback.Nudge, state.feedback)
        // The wrong number is cleared so the next try starts fresh.
        assertEquals("", state.input)
        assertEquals(1, state.wrongAttempts)
    }

    @Test
    fun `the second wrong answer offers the neighbouring fact as a strategy`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        val fact = viewModel.driveToFactWithNeighbor()

        repeat(2) {
            viewModel.type(99)
            viewModel.onIntent(TablesDrillIntent.Submit)
        }

        val strategy = assertInstanceOf(
            TablesFeedback.Strategy::class.java,
            viewModel.state.value.feedback,
        )
        assertEquals(fact.multiplicand - 1, strategy.neighbor.multiplicand)
        assertEquals(fact.table * (fact.multiplicand - 1), strategy.neighborProduct)
    }

    @Test
    fun `the third wrong answer reveals the product to copy`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        val fact = viewModel.state.value.currentCard!!.fact

        repeat(3) {
            viewModel.type(99)
            viewModel.onIntent(TablesDrillIntent.Submit)
        }

        val copy = assertInstanceOf(TablesFeedback.Copy::class.java, viewModel.state.value.feedback)
        assertEquals(fact.product, copy.answer)
    }

    @Test
    fun `copying the revealed answer still earns points and never punishes again`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        repeat(3) {
            viewModel.type(99)
            viewModel.onIntent(TablesDrillIntent.Submit)
        }
        // A wrong copy keeps the answer on screen rather than climbing further.
        viewModel.type(12)
        viewModel.onIntent(TablesDrillIntent.Submit)
        assertInstanceOf(TablesFeedback.Copy::class.java, viewModel.state.value.feedback)

        viewModel.answerCurrentCorrectly()
        advanceUntilIdle()

        val feedback = assertInstanceOf(
            TablesFeedback.Correct::class.java,
            viewModel.state.value.feedback,
        )
        assertEquals(PointValues.MATH_FACTS_COPY, feedback.pointsEarned)
    }

    @Test
    fun `a stumbled fact is requeued once so it comes back unaided`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        val stumbled = viewModel.state.value.currentCard!!

        viewModel.type(99)
        viewModel.onIntent(TablesDrillIntent.Submit)
        viewModel.answerCurrentCorrectly()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(11, state.total)
        assertEquals(stumbled.fact, state.cards.last().fact)
        // A retry pauses the combo instead of resetting it.
        assertTrue(state.comboPaused)
        assertEquals(0, state.combo)
    }

    @Test
    fun `stumbling the requeued copy does not queue it a second time`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Stumble the very first fact, so it is requeued at the end.
        val stumbled = viewModel.state.value.currentCard!!.fact
        viewModel.type(99)
        viewModel.onIntent(TablesDrillIntent.Submit)
        viewModel.answerCurrentCorrectly()
        advanceUntilIdle()
        assertEquals(11, viewModel.state.value.total)

        // Walk the remaining nine originals cleanly to reach the requeued copy.
        repeat(9) {
            viewModel.onIntent(TablesDrillIntent.Next)
            viewModel.answerCurrentCorrectly()
            advanceUntilIdle()
        }
        viewModel.onIntent(TablesDrillIntent.Next)
        assertEquals(stumbled, viewModel.state.value.currentCard!!.fact)

        // Stumbling it again must not grow the session a second time.
        viewModel.type(99)
        viewModel.onIntent(TablesDrillIntent.Submit)
        viewModel.answerCurrentCorrectly()
        advanceUntilIdle()

        assertEquals(11, viewModel.state.value.total)
    }

    @Test
    fun `a retry earns the middle tier, not the full first-try award`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.type(99)
        viewModel.onIntent(TablesDrillIntent.Submit)
        viewModel.answerCurrentCorrectly()
        advanceUntilIdle()

        val feedback = assertInstanceOf(
            TablesFeedback.Correct::class.java,
            viewModel.state.value.feedback,
        )
        assertEquals(PointValues.MATH_FACTS_RETRY, feedback.pointsEarned)
    }

    @Test
    fun `answering records the fact and reports growth`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        val fact = viewModel.state.value.currentCard!!.fact

        viewModel.answerCurrentCorrectly()
        advanceUntilIdle()

        coVerify {
            repository.recordAnswer(
                profileId = "default",
                table = fact.table,
                multiplicand = fact.multiplicand,
                correct = true,
                now = any(),
            )
        }
        assertEquals(1, viewModel.state.value.growths.size)
    }

    @Test
    fun `finishing the last card ends the session with the completion bonus`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        repeat(10) {
            viewModel.answerCurrentCorrectly()
            advanceUntilIdle()
            viewModel.onIntent(TablesDrillIntent.Next)
            advanceUntilIdle()
        }

        val state = viewModel.state.value
        assertEquals(TablesDrillPhase.RESULTS, state.phase)
        assertEquals(10, state.firstTryCount)
        assertEquals(
            10 * PointValues.MATH_FACTS_FIRST_TRY + PointValues.MATH_FACTS_SESSION_COMPLETE,
            state.sessionPoints,
        )
        coVerify {
            awardPointsUseCase(
                profileId = "default",
                basePoints = PointValues.MATH_FACTS_SESSION_COMPLETE,
                streak = 0,
                source = any(),
                reason = any(),
            )
        }
    }

    @Test
    fun `the bonus lap neither pays again nor reschedules the fact`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        val stumbled = viewModel.state.value.currentCard!!.fact

        // Stumble the first fact so it is requeued, then clear the rest.
        viewModel.type(99)
        viewModel.onIntent(TablesDrillIntent.Submit)
        viewModel.answerCurrentCorrectly()
        advanceUntilIdle()
        repeat(9) {
            viewModel.onIntent(TablesDrillIntent.Next)
            viewModel.answerCurrentCorrectly()
            advanceUntilIdle()
        }
        val pointsBeforeBonusLap = viewModel.state.value.sessionPoints

        viewModel.onIntent(TablesDrillIntent.Next)
        assertTrue(viewModel.state.value.isBonusLap)
        viewModel.answerCurrentCorrectly()
        advanceUntilIdle()

        // Answering it again must not pay: otherwise stumbling would be worth
        // more than knowing it (RETRY + FIRST_TRY > FIRST_TRY).
        val feedback = assertInstanceOf(
            TablesFeedback.Correct::class.java,
            viewModel.state.value.feedback,
        )
        assertEquals(0, feedback.pointsEarned)
        assertEquals(pointsBeforeBonusLap, viewModel.state.value.sessionPoints)

        // And it must not re-record: that would undo the lapse it just earned.
        coVerify(exactly = 1) {
            repository.recordAnswer(any(), stumbled.table, stumbled.multiplicand, false, any())
        }
        coVerify(exactly = 0) {
            repository.recordAnswer(any(), stumbled.table, stumbled.multiplicand, true, any())
        }
    }

    @Test
    fun `the progress denominator never grows when a fact is requeued`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(10, viewModel.state.value.plannedTotal)

        viewModel.type(99)
        viewModel.onIntent(TablesDrillIntent.Submit)
        viewModel.answerCurrentCorrectly()
        advanceUntilIdle()

        // The session grew, but what the child is counting towards did not.
        assertEquals(11, viewModel.state.value.total)
        assertEquals(10, viewModel.state.value.plannedTotal)
    }

    @Test
    fun `finishing twice does not award the session bonus twice`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        repeat(10) {
            viewModel.answerCurrentCorrectly()
            advanceUntilIdle()
            viewModel.onIntent(TablesDrillIntent.Next)
            advanceUntilIdle()
        }
        val settled = viewModel.state.value.sessionPoints

        // A fast double-tap on the last card must not re-run finish().
        repeat(3) {
            viewModel.onIntent(TablesDrillIntent.Next)
            advanceUntilIdle()
        }

        assertEquals(settled, viewModel.state.value.sessionPoints)
        coVerify(exactly = 1) {
            awardPointsUseCase(
                profileId = any(),
                basePoints = PointValues.MATH_FACTS_SESSION_COMPLETE,
                streak = any(),
                source = any(),
                reason = any(),
            )
        }
    }

    @Test
    fun `replay speaks the strategy again once the hint is showing`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        val fact = viewModel.driveToFactWithNeighbor()
        val neighbor = fact.hintNeighbor!!

        repeat(2) {
            viewModel.type(99)
            viewModel.onIntent(TablesDrillIntent.Submit)
        }
        val expected = "${neighbor.spokenPrompt}, ${neighbor.product}… alors ${fact.spokenPrompt} ?"
        verify(exactly = 1) { ttsManager.speak(expected, Locale.FRENCH, any()) }

        viewModel.onIntent(TablesDrillIntent.Replay)

        // Replay must re-speak the scaffold, not fall back to the bare question.
        verify(exactly = 2) { ttsManager.speak(expected, Locale.FRENCH, any()) }
    }

    @Test
    fun `a x1 fact has no neighbour so it goes straight to the reveal`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        val fact = viewModel.driveToTimesOneFact()

        repeat(2) {
            viewModel.type(99)
            viewModel.onIntent(TablesDrillIntent.Submit)
        }

        val copy = assertInstanceOf(TablesFeedback.Copy::class.java, viewModel.state.value.feedback)
        assertEquals(fact.product, copy.answer)
    }

    @Test
    fun `the keypad refuses a fourth digit`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.type(1234)

        assertEquals("123", viewModel.state.value.input)
    }

    @Test
    fun `playing again resets the session`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.answerCurrentCorrectly()
        advanceUntilIdle()

        viewModel.onIntent(TablesDrillIntent.PlayAgain)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(TablesDrillPhase.DRILLING, state.phase)
        assertEquals(0, state.index)
        assertEquals(0, state.sessionPoints)
        assertEquals(0, state.resolvedCount)
        assertEquals(10, state.plannedTotal)
        assertEquals(TablesFeedback.Idle, state.feedback)
    }

    @Test
    fun `an unknown table shows a friendly screen instead of crashing`() = runTest {
        val viewModel = createViewModel(table = "42")
        advanceUntilIdle()

        assertEquals(TablesDrillPhase.ERROR, viewModel.state.value.phase)
    }

    @Test
    fun `a missing mode argument shows a friendly screen instead of crashing`() = runTest {
        val viewModel = createViewModel(mode = "NOT_A_MODE")
        advanceUntilIdle()

        assertEquals(TablesDrillPhase.ERROR, viewModel.state.value.phase)
    }

    @Test
    fun `an empty submission does nothing`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(TablesDrillIntent.Submit)

        assertEquals(TablesFeedback.Idle, viewModel.state.value.feedback)
        assertEquals(0, viewModel.state.value.wrongAttempts)
    }

    @Test
    fun `a resolved card ignores further typing until Next`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.answerCurrentCorrectly()
        advanceUntilIdle()
        val resolvedInput = viewModel.state.value.input

        viewModel.onIntent(TablesDrillIntent.Digit(9))
        viewModel.onIntent(TablesDrillIntent.Backspace)

        assertEquals(resolvedInput, viewModel.state.value.input)
    }

    private companion object {
        val NOW = Instant.fromEpochMilliseconds(1_750_000_000_000)
    }
}
