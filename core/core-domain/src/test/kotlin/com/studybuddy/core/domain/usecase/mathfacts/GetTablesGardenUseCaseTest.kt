package com.studybuddy.core.domain.usecase.mathfacts

import com.studybuddy.core.domain.model.mathfacts.MathFactsRoster
import com.studybuddy.core.domain.model.srs.LeitnerGrowth
import com.studybuddy.core.domain.model.srs.LeitnerSchedule
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GetTablesGardenUseCaseTest {

    private val repository = FakeMathFactsReviewRepository()
    private val useCase = GetTablesGardenUseCase(repository)

    @Test
    fun `an untouched garden is all seeds with nothing due`() = runTest {
        val garden = useCase(TABLES_TEST_PROFILE, TABLES_TEST_NOW).first()

        assertEquals(0, garden.dueCardCount)
        assertEquals(0, garden.dueTableCount)
        assertEquals(MathFactsRoster.tables, garden.tables.map { it.table })
        assertTrue(garden.tables.all { it.growth == LeitnerGrowth.SEED })
    }

    @Test
    fun `a fully mastered table grows a tree, others stay seeds`() = runTest {
        repository.reviews = (1..10).map { multiplicand ->
            factReview(7, multiplicand, box = LeitnerSchedule.MAX_BOX, dueAt = TABLES_TEST_NOW + 5.days)
        }

        val garden = useCase(TABLES_TEST_PROFILE, TABLES_TEST_NOW).first()

        assertEquals(LeitnerGrowth.TREE, garden.tables.first { it.table == 7 }.growth)
        assertTrue(garden.tables.filter { it.table != 7 }.all { it.growth == LeitnerGrowth.SEED })
    }

    @Test
    fun `due counts count cards and distinct tables`() = runTest {
        repository.reviews = listOf(
            factReview(2, 3, dueAt = TABLES_TEST_NOW - 1.days),
            factReview(2, 4, dueAt = TABLES_TEST_NOW - 1.days),
            factReview(5, 6, dueAt = TABLES_TEST_NOW),
            // Not due yet — must not be counted.
            factReview(9, 9, dueAt = TABLES_TEST_NOW + 3.days),
        )

        val garden = useCase(TABLES_TEST_PROFILE, TABLES_TEST_NOW).first()

        assertEquals(3, garden.dueCardCount)
        assertEquals(2, garden.dueTableCount)
    }

    @Test
    fun `rows outside the roster are ignored everywhere`() = runTest {
        repository.reviews = listOf(factReview(13, 2, dueAt = TABLES_TEST_NOW - 1.days))

        val garden = useCase(TABLES_TEST_PROFILE, TABLES_TEST_NOW).first()

        assertEquals(0, garden.dueCardCount)
        assertTrue(garden.tables.none { it.table == 13 })
    }
}
