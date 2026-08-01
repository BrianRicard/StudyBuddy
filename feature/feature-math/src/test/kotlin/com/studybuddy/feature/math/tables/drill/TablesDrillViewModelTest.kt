package com.studybuddy.feature.math.tables.drill

import androidx.lifecycle.SavedStateHandle
import com.studybuddy.core.common.constants.PointValues
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

    /** TABLE mode gives a deterministic ten-card session for the table de 7. */
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
        val fact = viewModel.state.value.currentCard!!.fact

        repeat(2) {
            viewModel.type(99)
            viewModel.onIntent(TablesDrillIntent.Submit)
        }

        val feedback = viewModel.state.value.feedback
        if (fact.multiplicand > 1) {
            val strategy = assertInstanceOf(TablesFeedback.Strategy::class.java, feedback)
            assertEquals(fact.multiplicand - 1, strategy.neighbor.multiplicand)
            assertEquals(fact.table * (fact.multiplicand - 1), strategy.neighborProduct)
        } else {
            // A ×1 fact has no smaller neighbour, so it skips to the reveal.
            assertInstanceOf(TablesFeedback.Copy::class.java, feedback)
        }
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
