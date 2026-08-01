package com.studybuddy.feature.math.tables

import app.cash.turbine.test
import com.studybuddy.core.domain.model.mathfacts.MathFactReview
import com.studybuddy.core.domain.model.mathfacts.MathFactsRoster
import com.studybuddy.core.domain.model.srs.LeitnerGrowth
import com.studybuddy.core.domain.model.srs.LeitnerSchedule
import com.studybuddy.core.domain.repository.MathFactsReviewRepository
import com.studybuddy.core.domain.usecase.mathfacts.GetTablesGardenUseCase
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
import kotlinx.datetime.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TablesGardenViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository: MathFactsReviewRepository = mockk()

    /** Pinned so "is this card due?" never depends on when the suite runs. */
    private val clock = object : Clock {
        override fun now(): Instant = NOW
    }

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(reviews: List<MathFactReview> = emptyList()): TablesGardenViewModel {
        every { repository.getReviews("default") } returns flowOf(reviews)
        return TablesGardenViewModel(GetTablesGardenUseCase(repository, clock))
    }

    private fun review(
        table: Int,
        multiplicand: Int,
        box: Int = 2,
        overdue: Boolean = true,
    ) = MathFactReview(
        id = "$table-$multiplicand",
        profileId = "default",
        table = table,
        multiplicand = multiplicand,
        box = box,
        dueAt = NOW + if (overdue) (-1).days else 5.days,
        lapses = 0,
        updatedAt = NOW,
    )

    @Test
    fun `the garden starts loading and settles once the reviews arrive`() = runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            assertTrue(awaitItem().isLoading)
            assertFalse(awaitItem().isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loads the eight-table garden with due counts`() = runTest {
        val viewModel = createViewModel(
            reviews = listOf(
                review(2, 3),
                review(2, 4),
                review(7, 8),
            ),
        )
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals((2..9).toList(), state.tables.map { it.table })
        assertEquals(3, state.dueCardCount)
        assertEquals(2, state.dueTableCount)
    }

    @Test
    fun `cards that are not due yet are not counted`() = runTest {
        val viewModel = createViewModel(
            reviews = (1..10).map { m -> review(4, m, overdue = false) },
        )
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(0, state.dueCardCount)
        assertEquals(0, state.dueTableCount)
    }

    @Test
    fun `a fresh garden has every fact still to plant, not nothing to do`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(0, state.dueCardCount)
        // The whole roster is unplanted, so Révision still has work to offer.
        assertEquals(MathFactsRoster.all.size, state.newCardCount)
        assertTrue(state.tables.all { it.growth == LeitnerGrowth.SEED })
    }

    @Test
    fun `planted facts drop out of the new count`() = runTest {
        val viewModel = createViewModel(
            reviews = (1..10).map { m -> review(5, m, overdue = false) },
        )
        advanceUntilIdle()

        assertEquals(MathFactsRoster.all.size - 10, viewModel.state.value.newCardCount)
    }

    @Test
    fun `a fully mastered table shows a tree`() = runTest {
        val viewModel = createViewModel(
            reviews = (1..10).map { m ->
                review(5, m, box = LeitnerSchedule.MAX_BOX, overdue = false)
            },
        )
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(LeitnerGrowth.TREE, state.tables.first { it.table == 5 }.growth)
        assertTrue(state.tables.filter { it.table != 5 }.all { it.growth == LeitnerGrowth.SEED })
    }

    @Test
    fun `every intent shows the coming-soon note until the drill ships`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(TablesGardenIntent.StartRevision)
            assertEquals(TablesGardenEffect.ShowComingSoon, awaitItem())

            viewModel.onIntent(TablesGardenIntent.StartSurprise)
            assertEquals(TablesGardenEffect.ShowComingSoon, awaitItem())

            viewModel.onIntent(TablesGardenIntent.OpenTable(7))
            assertEquals(TablesGardenEffect.ShowComingSoon, awaitItem())
        }
    }

    private companion object {
        val NOW = Instant.fromEpochMilliseconds(1_750_000_000_000)
    }
}
