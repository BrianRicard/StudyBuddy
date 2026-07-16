package com.studybuddy.feature.conjugation.boss

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.studybuddy.core.common.constants.PointValues
import com.studybuddy.core.domain.model.conjugation.ConjugationStep
import com.studybuddy.core.domain.repository.ConjugationRepository
import com.studybuddy.core.domain.repository.StepResultOutcome
import com.studybuddy.core.domain.usecase.points.AwardPointsUseCase
import com.studybuddy.shared.tts.TtsManager
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BossViewModelTest {

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

    private fun createViewModel(stageId: String = "etre") = BossViewModel(
        savedStateHandle = SavedStateHandle(mapOf("stageId" to stageId)),
        conjugationRepository = conjugationRepository,
        awardPointsUseCase = awardPointsUseCase,
        ttsManager = ttsManager,
    )

    /** Rebuilds the current sentence by always tapping the expected chip. */
    private fun BossViewModel.solveCurrentSentence() {
        while (state.value.phase == BossPhase.BUILD) {
            val current = state.value
            val expected = current.targetWords[current.builtWords.size]
            val chip = current.bank.first { !it.isUsed && it.text == expected }
            onIntent(BossIntent.TapChip(chip.id))
        }
    }

    @Test
    fun `starts with an intro and the stage boss`() {
        val viewModel = createViewModel()

        val state = viewModel.state.value
        assertEquals(BossPhase.INTRO, state.phase)
        assertEquals(3, state.sentences.size)
        assertEquals("frog", state.stage?.bossCharacterId)
    }

    @Test
    fun `starting the rap shuffles the word bank for the first sentence`() {
        val viewModel = createViewModel()

        viewModel.onIntent(BossIntent.StartBuilding)

        val state = viewModel.state.value
        assertEquals(BossPhase.BUILD, state.phase)
        assertEquals(state.targetWords.sorted(), state.bank.map { it.text }.sorted())
    }

    @Test
    fun `tapping the right words rebuilds the sentence`() = runTest {
        val viewModel = createViewModel()
        viewModel.onIntent(BossIntent.StartBuilding)

        viewModel.solveCurrentSentence()

        val state = viewModel.state.value
        assertEquals(BossPhase.SENTENCE_DONE, state.phase)
        assertEquals(state.targetWords, state.builtWords)
        assertEquals(0, state.mistakes)
    }

    @Test
    fun `a wrong chip shakes gently and costs nothing but a mistake count`() = runTest {
        val viewModel = createViewModel()
        viewModel.onIntent(BossIntent.StartBuilding)

        val current = viewModel.state.value
        val expected = current.targetWords.first()
        val wrongChip = current.bank.first { it.text != expected }
        viewModel.onIntent(BossIntent.TapChip(wrongChip.id))

        val state = viewModel.state.value
        assertEquals(BossPhase.BUILD, state.phase)
        assertEquals(1, state.mistakes)
        assertEquals(wrongChip.id, state.shakeChipId)
        assertTrue(state.builtWords.isEmpty())
    }

    @Test
    fun `completing all three sentences wins the rap battle`() = runTest {
        val viewModel = createViewModel()
        viewModel.onIntent(BossIntent.StartBuilding)

        repeat(3) {
            viewModel.solveCurrentSentence()
            viewModel.onIntent(BossIntent.NextSentence)
        }

        assertEquals(BossPhase.WON, viewModel.state.value.phase)
    }

    @Test
    fun `finishing records the score and awards the stage completion bonus`() = runTest {
        val viewModel = createViewModel()
        viewModel.onIntent(BossIntent.StartBuilding)
        val totalWords = viewModel.state.value.totalWords

        viewModel.effects.test {
            repeat(3) {
                viewModel.solveCurrentSentence()
                viewModel.onIntent(BossIntent.NextSentence)
            }
            viewModel.onIntent(BossIntent.Finish)
            advanceUntilIdle()

            assertEquals(BossEffect.Completed, awaitItem())
        }

        coVerify {
            conjugationRepository.recordStepResult(
                profileId = "default",
                stageId = "etre",
                step = ConjugationStep.BOSS,
                correct = totalWords,
                total = totalWords,
            )
        }
        coVerify {
            awardPointsUseCase(
                profileId = "default",
                basePoints = PointValues.CONJUGATION_STAGE_COMPLETE,
                streak = 0,
                source = any(),
                reason = any(),
            )
        }
    }

    @Test
    fun `the final stage is flagged for the grumpy king finale`() {
        val viewModel = createViewModel(stageId = "dire")

        assertTrue(viewModel.state.value.isFinalStage)
        assertEquals("lion", viewModel.state.value.stage?.bossCharacterId)
    }
}
