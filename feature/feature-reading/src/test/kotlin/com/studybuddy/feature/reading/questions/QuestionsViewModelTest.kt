package com.studybuddy.feature.reading.questions

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.studybuddy.core.domain.model.ReadingPassage
import com.studybuddy.core.domain.model.ReadingQuestion
import com.studybuddy.core.domain.model.ReadingQuestionType
import com.studybuddy.core.domain.model.ReadingTheme
import com.studybuddy.core.domain.repository.ReadingRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuestionsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val readingRepository: ReadingRepository = mockk()

    private val testQuestions = listOf(
        ReadingQuestion(
            id = "p1_q1",
            passageId = "p1",
            questionIndex = 0,
            type = ReadingQuestionType.MULTIPLE_CHOICE,
            questionText = "What did the tortoise do?",
            options = listOf("Run", "Walk slowly", "Sleep", "Fly"),
            correctAnswer = "Walk slowly",
            explanation = "The tortoise walked slowly but steadily.",
            evidenceSentenceIndex = 1,
        ),
        ReadingQuestion(
            id = "p1_q2",
            passageId = "p1",
            questionIndex = 1,
            type = ReadingQuestionType.TRUE_FALSE,
            questionText = "The hare won the race.",
            options = null,
            correctAnswer = "false",
            explanation = "The tortoise won because the hare napped.",
            evidenceSentenceIndex = 3,
        ),
        ReadingQuestion(
            id = "p1_q3",
            passageId = "p1",
            questionIndex = 2,
            type = ReadingQuestionType.FIND_IN_TEXT,
            questionText = "Which sentence shows the hare was overconfident?",
            options = null,
            correctAnswer = "2",
            explanation = "Sentence 2 shows overconfidence.",
            evidenceSentenceIndex = 2,
        ),
    )

    private val testPassage = ReadingPassage(
        id = "p1",
        language = "EN",
        tier = 1,
        theme = ReadingTheme.ANIMALS,
        title = "The Tortoise and the Hare",
        passage = "Sentence one. Sentence two. Sentence three. Sentence four.",
        wordCount = 50,
        source = "public_domain",
        sourceAttribution = "Aesop's Fables",
        questions = testQuestions,
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { readingRepository.getPassageById("p1") } returns testPassage
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = QuestionsViewModel(
        savedStateHandle = SavedStateHandle(
            mapOf("passageId" to "p1", "readingTimeMs" to 5000L),
        ),
        readingRepository = readingRepository,
    )

    @Test
    fun `init loads passage and sets loading false`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        val state = vm.state.value
        assertFalse(state.isLoading)
        assertEquals("p1", state.passage?.id)
        assertEquals(3, state.totalQuestions)
        assertEquals(0, state.currentQuestionIndex)
    }

    @Test
    fun `selecting answer updates selectedAnswer`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onIntent(QuestionsIntent.SelectAnswer("Walk slowly"))

        assertEquals("Walk slowly", vm.state.value.selectedAnswer)
    }

    @Test
    fun `confirming correct answer marks it correct`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onIntent(QuestionsIntent.SelectAnswer("Walk slowly"))
        vm.onIntent(QuestionsIntent.ConfirmAnswer)

        val state = vm.state.value
        assertTrue(state.isAnswerRevealed)
        assertEquals(1, state.answers.size)
        assertTrue(state.answers[0].isCorrect)
    }

    @Test
    fun `confirming wrong answer marks it incorrect`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onIntent(QuestionsIntent.SelectAnswer("Run"))
        vm.onIntent(QuestionsIntent.ConfirmAnswer)

        assertFalse(vm.state.value.answers[0].isCorrect)
    }

    @Test
    fun `cannot select answer after reveal`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onIntent(QuestionsIntent.SelectAnswer("Walk slowly"))
        vm.onIntent(QuestionsIntent.ConfirmAnswer)
        vm.onIntent(QuestionsIntent.SelectAnswer("Run"))

        assertEquals("Walk slowly", vm.state.value.selectedAnswer)
    }

    @Test
    fun `next question advances index and clears selection`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onIntent(QuestionsIntent.SelectAnswer("Walk slowly"))
        vm.onIntent(QuestionsIntent.ConfirmAnswer)
        vm.onIntent(QuestionsIntent.NextQuestion)

        val state = vm.state.value
        assertEquals(1, state.currentQuestionIndex)
        assertNull(state.selectedAnswer)
        assertFalse(state.isAnswerRevealed)
    }

    @Test
    fun `toggle passage toggles expanded state`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        assertFalse(vm.state.value.showPassageExpanded)
        vm.onIntent(QuestionsIntent.TogglePassage)
        assertTrue(vm.state.value.showPassageExpanded)
        vm.onIntent(QuestionsIntent.TogglePassage)
        assertFalse(vm.state.value.showPassageExpanded)
    }

    @Test
    fun `changing answer before confirm tracks changedAnswer`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onIntent(QuestionsIntent.SelectAnswer("Run"))
        vm.onIntent(QuestionsIntent.SelectAnswer("Walk slowly"))
        vm.onIntent(QuestionsIntent.ConfirmAnswer)

        assertTrue(vm.state.value.answers[0].changedAnswer)
    }

    @Test
    fun `not changing answer has changedAnswer false`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onIntent(QuestionsIntent.SelectAnswer("Walk slowly"))
        vm.onIntent(QuestionsIntent.ConfirmAnswer)

        assertFalse(vm.state.value.answers[0].changedAnswer)
    }

    @Test
    fun `finishing last question emits NavigateToResults`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.effects.test {
            // Answer all 3 questions
            vm.onIntent(QuestionsIntent.SelectAnswer("Walk slowly"))
            vm.onIntent(QuestionsIntent.ConfirmAnswer)
            vm.onIntent(QuestionsIntent.NextQuestion)

            vm.onIntent(QuestionsIntent.SelectAnswer("false"))
            vm.onIntent(QuestionsIntent.ConfirmAnswer)
            vm.onIntent(QuestionsIntent.NextQuestion)

            vm.onIntent(QuestionsIntent.SelectAnswer("2"))
            vm.onIntent(QuestionsIntent.ConfirmAnswer)
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is QuestionsEffect.NavigateToResults)
            val results = effect as QuestionsEffect.NavigateToResults
            assertEquals(3, results.score)
            assertEquals(3, results.totalQuestions)
            assertTrue(results.allCorrectFirstTry)
            assertEquals(1, results.tier)
        }
    }
}
