package com.studybuddy.feature.conjugation.atelier

import app.cash.turbine.test
import com.studybuddy.core.domain.model.conjugation.AtelierGrowth
import com.studybuddy.core.domain.model.conjugation.AtelierReview
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson
import com.studybuddy.core.domain.model.conjugation.ConjugationTense
import com.studybuddy.core.domain.model.conjugation.FrenchVerbs
import com.studybuddy.core.domain.repository.AtelierReviewRepository
import com.studybuddy.core.domain.usecase.conjugation.GetAtelierGardenUseCase
import io.mockk.every
import io.mockk.mockk
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AtelierViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository: AtelierReviewRepository = mockk()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(reviews: List<AtelierReview> = emptyList()): AtelierViewModel {
        every { repository.getReviews("default") } returns flowOf(reviews)
        return AtelierViewModel(GetAtelierGardenUseCase(repository))
    }

    private fun dueReview(
        verbId: String,
        person: ConjugationPerson,
    ) = AtelierReview(
        id = "$verbId-$person",
        profileId = "default",
        verbId = verbId,
        tense = ConjugationTense.PRESENT,
        person = person,
        box = 2,
        dueAt = Clock.System.now() - 1.days,
        lapses = 0,
        updatedAt = Clock.System.now(),
    )

    @Test
    fun `loads the full garden with due counts`() = runTest {
        val viewModel = createViewModel(
            reviews = listOf(
                dueReview("etre", ConjugationPerson.JE),
                dueReview("etre", ConjugationPerson.TU),
                dueReview("avoir", ConjugationPerson.JE),
            ),
        )
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(!state.isLoading)
        assertEquals(FrenchVerbs.all.size, state.verbs.size)
        assertEquals(3, state.dueCardCount)
        assertEquals(2, state.dueVerbCount)
        assertEquals(
            AtelierGrowth.SPROUT,
            state.verbs.first { it.verb.id == "etre" }.growth[ConjugationTense.PRESENT],
        )
    }

    @Test
    fun `an empty garden is all seeds and not loading`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(!state.isLoading)
        assertEquals(0, state.dueCardCount)
        assertTrue(
            state.verbs.all { row -> row.growth.values.all { it == AtelierGrowth.SEED } },
        )
    }

    @Test
    fun `every intent shows the coming-soon note until the drill ships`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(AtelierIntent.StartRevision)
            assertEquals(AtelierEffect.ShowComingSoon, awaitItem())

            viewModel.onIntent(AtelierIntent.StartSurprise)
            assertEquals(AtelierEffect.ShowComingSoon, awaitItem())

            viewModel.onIntent(AtelierIntent.OpenCell("etre", ConjugationTense.FUTUR))
            assertEquals(AtelierEffect.ShowComingSoon, awaitItem())
        }
    }
}
