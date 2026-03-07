package com.studybuddy.feature.reading.results

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.studybuddy.core.domain.model.PointSource
import com.studybuddy.core.domain.model.ReadingPassage
import com.studybuddy.core.domain.model.ReadingTheme
import com.studybuddy.core.domain.repository.ReadingRepository
import com.studybuddy.core.domain.usecase.points.AwardPointsUseCase
import com.studybuddy.shared.points.RewardCalculator
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
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReadingResultsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val readingRepository: ReadingRepository = mockk()
    private val awardPointsUseCase: AwardPointsUseCase = mockk()
    private val rewardCalculator = RewardCalculator()

    private val testPassage = ReadingPassage(
        id = "p1",
        language = "EN",
        tier = 2,
        theme = ReadingTheme.ANIMALS,
        title = "Test Passage",
        passage = "Content",
        wordCount = 50,
        source = "test",
        sourceAttribution = null,
        questions = emptyList(),
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { readingRepository.getPassageById("p1") } returns testPassage
        coEvery { readingRepository.saveResult(any()) } returns Unit
        coEvery { awardPointsUseCase(any(), any(), any(), any(), any()) } returns Unit
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        score: Int = 3,
        totalQuestions: Int = 4,
        allCorrectFirstTry: Boolean = false,
        tier: Int = 2,
    ) = ReadingResultsViewModel(
        savedStateHandle = SavedStateHandle(
            mapOf(
                "passageId" to "p1",
                "score" to score,
                "totalQuestions" to totalQuestions,
                "readingTimeMs" to 30000L,
                "questionsTimeMs" to 20000L,
                "allCorrectFirstTry" to allCorrectFirstTry,
                "tier" to tier,
            ),
        ),
        readingRepository = readingRepository,
        rewardCalculator = rewardCalculator,
        awardPointsUseCase = awardPointsUseCase,
    )

    @Test
    fun `init calculates reward and saves result`() = runTest {
        val vm = createViewModel(score = 3, totalQuestions = 4, tier = 2)
        advanceUntilIdle()

        val state = vm.state.value
        assertFalse(state.isLoading)
        assertEquals(3, state.score)
        assertEquals(4, state.totalQuestions)
        assertEquals("Test Passage", state.passageTitle)
        assertTrue(state.pointsEarned > 0)

        coVerify { readingRepository.saveResult(any()) }
        coVerify { awardPointsUseCase(any(), any(), any(), eq(PointSource.READING), any()) }
    }

    @Test
    fun `perfect score with first try bonus earns more`() = runTest {
        val vmNoBonus = createViewModel(score = 4, totalQuestions = 4, allCorrectFirstTry = false, tier = 1)
        advanceUntilIdle()
        val noBonus = vmNoBonus.state.value.pointsEarned

        val vmBonus = createViewModel(score = 4, totalQuestions = 4, allCorrectFirstTry = true, tier = 1)
        advanceUntilIdle()
        val withBonus = vmBonus.state.value.pointsEarned

        assertEquals(RewardCalculator.READING_FIRST_TRY_BONUS, withBonus - noBonus)
    }

    @Test
    fun `read again emits NavigateToPassage`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.effects.test {
            vm.onIntent(ReadingResultsIntent.ReadAgain)
            val effect = awaitItem()
            assertTrue(effect is ReadingResultsEffect.NavigateToPassage)
            assertEquals("p1", (effect as ReadingResultsEffect.NavigateToPassage).passageId)
        }
    }

    @Test
    fun `go home emits NavigateHome`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.effects.test {
            vm.onIntent(ReadingResultsIntent.GoHome)
            assertTrue(awaitItem() is ReadingResultsEffect.NavigateHome)
        }
    }
}
