package com.studybuddy.feature.conjugation.path

import app.cash.turbine.test
import com.studybuddy.core.domain.model.conjugation.ConjugationProgress
import com.studybuddy.core.domain.model.conjugation.ConjugationStep
import com.studybuddy.core.domain.repository.ConjugationRepository
import com.studybuddy.core.domain.usecase.conjugation.GetConjugationPathUseCase
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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConjugationPathViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository: ConjugationRepository = mockk()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun completedStep(
        stageId: String,
        step: ConjugationStep,
    ) = ConjugationProgress(
        id = "$stageId-$step",
        profileId = "default",
        stageId = stageId,
        step = step,
        bestCorrect = 6,
        bestTotal = 6,
        completedAt = Instant.fromEpochMilliseconds(1_000),
        updatedAt = Instant.fromEpochMilliseconds(1_000),
    )

    private fun createViewModel(progress: List<ConjugationProgress> = emptyList()): ConjugationPathViewModel {
        every { repository.getProgressForProfile("default") } returns flowOf(progress)
        return ConjugationPathViewModel(GetConjugationPathUseCase(repository))
    }

    @Test
    fun `loads the six stages and auto-opens the next playable one`() = runTest {
        val viewModel = createViewModel(
            progress = ConjugationStep.entries.map { completedStep("etre", it) },
        )
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(6, state.stages.size)
        // Stage 1 done, so stage 2 (avoir) opens automatically.
        assertEquals("avoir", state.expandedStageId)
    }

    @Test
    fun `toggling a stage collapses and expands it`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals("etre", viewModel.state.value.expandedStageId)

        viewModel.onIntent(ConjugationPathIntent.ToggleStage("etre"))
        assertNull(viewModel.state.value.expandedStageId)

        viewModel.onIntent(ConjugationPathIntent.ToggleStage("aimer"))
        assertEquals("aimer", viewModel.state.value.expandedStageId)
    }

    @Test
    fun `opening an unlocked step navigates to it`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(ConjugationPathIntent.OpenStep("etre", ConjugationStep.LEARN))
            assertEquals(
                ConjugationPathEffect.NavigateToStep("etre", ConjugationStep.LEARN),
                awaitItem(),
            )
        }
    }

    @Test
    fun `locked steps and stages never navigate`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            // WRITE is locked until LEARN is done; stage 2 is fully locked.
            viewModel.onIntent(ConjugationPathIntent.OpenStep("etre", ConjugationStep.WRITE))
            viewModel.onIntent(ConjugationPathIntent.OpenStep("avoir", ConjugationStep.LEARN))
            advanceUntilIdle()
            expectNoEvents()
        }
    }
}
