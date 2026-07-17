package com.studybuddy.feature.conjugation.write

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.studybuddy.core.domain.model.conjugation.ConjugationStep
import com.studybuddy.core.domain.repository.ConjugationRepository
import com.studybuddy.core.domain.repository.StepResultOutcome
import com.studybuddy.core.domain.usecase.conjugation.CheckConjugationAnswerUseCase
import com.studybuddy.core.domain.usecase.points.AwardPointsUseCase
import com.studybuddy.shared.tts.TtsManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WriteViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val conjugationRepository: ConjugationRepository = mockk()
    private val awardPointsUseCase: AwardPointsUseCase = mockk(relaxed = true)
    private val ttsManager: TtsManager = mockk(relaxed = true)

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery {
            conjugationRepository.recordStepResult(any(), any(), any(), any(), any())
        } returns StepResultOutcome(firstCompletion = true, newBest = true)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(stageId: String = "etre") = WriteViewModel(
        savedStateHandle = SavedStateHandle(mapOf("stageId" to stageId)),
        checkAnswer = CheckConjugationAnswerUseCase(),
        conjugationRepository = conjugationRepository,
        awardPointsUseCase = awardPointsUseCase,
        ttsManager = ttsManager,
    )

    @Test
    fun `starts at the first person with the stage loaded`() {
        val viewModel = createViewModel()

        val state = viewModel.state.value
        assertEquals("etre", state.stage?.id)
        assertEquals("je", state.person.pronoun)
        assertEquals(WriteFeedback.Idle, state.feedback)
    }

    @Test
    fun `a correct answer praises, counts and plays the form`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(WriteIntent.InputChanged("suis"))
        viewModel.onIntent(WriteIntent.Submit)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.feedback is WriteFeedback.Correct)
        assertEquals(1, state.firstTryCorrect)
        verify { ttsManager.speak("je suis", any(), any()) }
    }

    @Test
    fun `accents are lenient by default`() = runTest {
        val viewModel = createViewModel()
        // Move to VOUS (index 4): êtes
        repeat(4) {
            val current = viewModel.state.value
            viewModel.onIntent(WriteIntent.InputChanged(current.stage!!.verb.form(current.person)))
            viewModel.onIntent(WriteIntent.Submit)
            advanceUntilIdle()
            viewModel.onIntent(WriteIntent.Next)
        }

        viewModel.onIntent(WriteIntent.InputChanged("etes"))
        viewModel.onIntent(WriteIntent.Submit)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.feedback is WriteFeedback.Correct)
    }

    @Test
    fun `a wrong answer encourages and never counts against the child`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(WriteIntent.InputChanged("sui"))
        viewModel.onIntent(WriteIntent.Submit)

        val state = viewModel.state.value
        assertEquals(WriteFeedback.TryAgain, state.feedback)
        assertEquals(1, state.attemptsOnCurrent)
        assertFalse(state.canRevealHint)
    }

    @Test
    fun `the hint unlocks after two attempts and reveals the spelling`() = runTest {
        val viewModel = createViewModel()

        repeat(2) {
            viewModel.onIntent(WriteIntent.InputChanged("wrong$it"))
            viewModel.onIntent(WriteIntent.Submit)
        }
        assertTrue(viewModel.state.value.canRevealHint)

        viewModel.onIntent(WriteIntent.RevealHint)

        assertEquals(WriteFeedback.Revealed("suis"), viewModel.state.value.feedback)
    }

    @Test
    fun `a corrected answer no longer counts as first try`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(WriteIntent.InputChanged("sui"))
        viewModel.onIntent(WriteIntent.Submit)
        viewModel.onIntent(WriteIntent.InputChanged("suis"))
        viewModel.onIntent(WriteIntent.Submit)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.feedback is WriteFeedback.Correct)
        assertEquals(0, state.firstTryCorrect)
    }

    @Test
    fun `finishing all six forms records the result and completes`() = runTest {
        val viewModel = createViewModel()

        viewModel.effects.test {
            repeat(6) {
                val form = viewModel.state.value.stage!!.verb.form(viewModel.state.value.person)
                viewModel.onIntent(WriteIntent.InputChanged(form))
                viewModel.onIntent(WriteIntent.Submit)
                advanceUntilIdle()
                viewModel.onIntent(WriteIntent.Next)
                advanceUntilIdle()
            }

            assertEquals(WriteEffect.Completed, awaitItem())
        }

        coVerify {
            conjugationRepository.recordStepResult(
                profileId = "default",
                stageId = "etre",
                step = ConjugationStep.WRITE,
                correct = 6,
                total = 6,
            )
        }
    }
}
