package com.studybuddy.feature.conjugation.battle

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.studybuddy.core.domain.model.conjugation.ConjugationStep
import com.studybuddy.core.domain.repository.ConjugationRepository
import com.studybuddy.core.domain.repository.StepResultOutcome
import com.studybuddy.core.domain.usecase.conjugation.BuildBattleRoundsUseCase
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
class BattleViewModelTest {

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

    private fun createViewModel(stageId: String = "etre") = BattleViewModel(
        savedStateHandle = SavedStateHandle(mapOf("stageId" to stageId)),
        buildBattleRounds = BuildBattleRoundsUseCase(),
        conjugationRepository = conjugationRepository,
        awardPointsUseCase = awardPointsUseCase,
        ttsManager = ttsManager,
    )

    @Test
    fun `stage 1 battle has six rounds and starts on a question`() {
        val viewModel = createViewModel()

        val state = viewModel.state.value
        assertEquals(6, state.totalRounds)
        assertEquals(BattlePhase.QUESTION, state.phase)
        assertEquals(0f, state.cheerProgress)
    }

    @Test
    fun `a correct pick fills the cheer meter`() = runTest {
        val viewModel = createViewModel()
        val round = viewModel.state.value.currentRound!!

        viewModel.onIntent(BattleIntent.SelectOption(round.correctForm))

        val state = viewModel.state.value
        assertEquals(BattlePhase.CORRECT, state.phase)
        assertEquals(1, state.cheeredCount)
        assertEquals(1, state.firstTryCorrect)
    }

    @Test
    fun `a wrong pick encourages and requeues the question`() = runTest {
        val viewModel = createViewModel()
        val round = viewModel.state.value.currentRound!!
        val wrong = round.options.first { it != round.correctForm }

        viewModel.onIntent(BattleIntent.SelectOption(wrong))
        assertEquals(BattlePhase.ENCOURAGE, viewModel.state.value.phase)

        viewModel.onIntent(BattleIntent.Continue)

        val state = viewModel.state.value
        assertEquals(BattlePhase.QUESTION, state.phase)
        // The missed round went to the back of the queue: nothing is lost.
        assertEquals(6, state.queue.size)
        assertEquals(round, state.queue.last())
        assertEquals(0, state.cheeredCount)
    }

    @Test
    fun `answering every round wins the battle with a gift`() = runTest {
        val viewModel = createViewModel()

        repeat(6) {
            val round = viewModel.state.value.currentRound!!
            viewModel.onIntent(BattleIntent.SelectOption(round.correctForm))
            viewModel.onIntent(BattleIntent.Continue)
        }

        assertEquals(BattlePhase.GIFT, viewModel.state.value.phase)
        advanceUntilIdle()
        assertEquals(BattlePhase.WON, viewModel.state.value.phase)
        assertEquals(1f, viewModel.state.value.cheerProgress)
    }

    @Test
    fun `a battle with misses still ends in victory`() = runTest {
        val viewModel = createViewModel()

        // Miss the first round once, then answer everything correctly.
        val first = viewModel.state.value.currentRound!!
        viewModel.onIntent(BattleIntent.SelectOption(first.options.first { it != first.correctForm }))
        viewModel.onIntent(BattleIntent.Continue)

        repeat(6) {
            val round = viewModel.state.value.currentRound!!
            viewModel.onIntent(BattleIntent.SelectOption(round.correctForm))
            viewModel.onIntent(BattleIntent.Continue)
        }
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(BattlePhase.WON, state.phase)
        assertEquals(5, state.firstTryCorrect)
    }

    @Test
    fun `finishing records the first-try score and awards points`() = runTest {
        val viewModel = createViewModel()

        viewModel.effects.test {
            repeat(6) {
                val round = viewModel.state.value.currentRound!!
                viewModel.onIntent(BattleIntent.SelectOption(round.correctForm))
                viewModel.onIntent(BattleIntent.Continue)
            }
            advanceUntilIdle()
            viewModel.onIntent(BattleIntent.Finish)
            advanceUntilIdle()

            assertEquals(BattleEffect.Completed, awaitItem())
        }

        coVerify {
            conjugationRepository.recordStepResult(
                profileId = "default",
                stageId = "etre",
                step = ConjugationStep.BATTLE,
                correct = 6,
                total = 6,
            )
        }
        coVerify { awardPointsUseCase(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `later stages include review rounds`() {
        val viewModel = createViewModel(stageId = "dire")

        val state = viewModel.state.value
        assertEquals(8, state.totalRounds)
        assertTrue(state.queue.any { it.isReview })
    }
}
