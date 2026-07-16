package com.studybuddy.feature.conjugation.learn

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson
import com.studybuddy.core.domain.model.conjugation.ConjugationStep
import com.studybuddy.core.domain.repository.ConjugationRepository
import com.studybuddy.core.domain.repository.StepResultOutcome
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
class LearnViewModelTest {

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

    private fun createViewModel() = LearnViewModel(
        savedStateHandle = SavedStateHandle(mapOf("stageId" to "avoir")),
        ttsManager = ttsManager,
        conjugationRepository = conjugationRepository,
        awardPointsUseCase = awardPointsUseCase,
    )

    @Test
    fun `playing a form speaks it with elision and marks it heard`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(LearnIntent.PlayForm(ConjugationPerson.JE))

        verify { ttsManager.speak("j'ai", any(), any()) }
        assertTrue(ConjugationPerson.JE in viewModel.state.value.heard)
        assertFalse(viewModel.state.value.allHeard)
    }

    @Test
    fun `finish is ignored until every form was heard`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(LearnIntent.Finish)
        advanceUntilIdle()

        coVerify(exactly = 0) {
            conjugationRepository.recordStepResult(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `hearing all six forms unlocks finishing and records the step`() = runTest {
        val viewModel = createViewModel()
        ConjugationPerson.entries.forEach { viewModel.onIntent(LearnIntent.PlayForm(it)) }
        assertTrue(viewModel.state.value.allHeard)

        viewModel.effects.test {
            viewModel.onIntent(LearnIntent.Finish)
            advanceUntilIdle()
            assertEquals(LearnEffect.Completed, awaitItem())
        }

        coVerify {
            conjugationRepository.recordStepResult(
                profileId = "default",
                stageId = "avoir",
                step = ConjugationStep.LEARN,
                correct = 6,
                total = 6,
            )
        }
    }
}
