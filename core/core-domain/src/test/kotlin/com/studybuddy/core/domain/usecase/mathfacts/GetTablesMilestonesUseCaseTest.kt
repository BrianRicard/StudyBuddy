package com.studybuddy.core.domain.usecase.mathfacts

import com.studybuddy.core.domain.model.mathfacts.MathFactsMilestone
import com.studybuddy.core.domain.model.mathfacts.MathFactsRoster
import com.studybuddy.core.domain.model.srs.LeitnerSchedule
import kotlin.time.Duration.Companion.days
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GetTablesMilestonesUseCaseTest {

    private val useCase = GetTablesMilestonesUseCase()

    /** Every fact of [table] at the top box, updated [at]. */
    private fun masteredTable(
        table: Int,
        at: Instant,
    ) = MathFactsRoster.factsOf(table).map { fact ->
        factReview(
            table = fact.table,
            multiplicand = fact.multiplicand,
            box = LeitnerSchedule.MAX_BOX,
            dueAt = at,
        )
    }

    private fun statusOf(
        reviews: List<com.studybuddy.core.domain.model.mathfacts.MathFactReview>,
        milestone: MathFactsMilestone,
    ) = useCase(reviews).single { it.milestone == milestone }

    @Test
    fun `an empty garden has nothing achieved`() {
        val statuses = useCase(emptyList())

        assertEquals(MathFactsMilestone.entries.size, statuses.size)
        assertTrue(statuses.none { it.isAchieved })
        assertTrue(statuses.all { it.current == 0 })
    }

    @Test
    fun `a single top-box fact clinches the first-fact milestone`() {
        val reviews = listOf(
            factReview(7, 8, box = LeitnerSchedule.MAX_BOX, dueAt = TABLES_TEST_NOW),
            factReview(7, 9, box = 1, dueAt = TABLES_TEST_NOW),
        )

        val first = statusOf(reviews, MathFactsMilestone.FIRST_FACT_MASTERED)

        assertTrue(first.isAchieved)
        assertEquals(1, first.current)
        assertEquals(TABLES_TEST_NOW, first.achievedAt)
    }

    @Test
    fun `a partly-learned table is not a mastered table`() {
        // Nine of ten facts at the top box: close, but not a table.
        val reviews = masteredTable(7, TABLES_TEST_NOW).drop(1)

        val table = statusOf(reviews, MathFactsMilestone.FIRST_TABLE_MASTERED)

        assertFalse(table.isAchieved)
        assertEquals(0, table.current)
        assertNull(table.achievedAt)
    }

    @Test
    fun `a fully mastered table is timestamped by the fact that clinched it`() {
        val earlier = TABLES_TEST_NOW
        val clinching = TABLES_TEST_NOW + 3.days
        val reviews = masteredTable(7, earlier).dropLast(1) +
            factReview(7, MathFactsRoster.LAST_MULTIPLICAND, box = LeitnerSchedule.MAX_BOX, dueAt = clinching)

        val table = statusOf(reviews, MathFactsMilestone.FIRST_TABLE_MASTERED)

        assertTrue(table.isAchieved)
        assertEquals(clinching, table.achievedAt)
    }

    @Test
    fun `four mastered tables clinch the four-table milestone but not the last one`() {
        val reviews = listOf(2, 3, 4, 5).flatMapIndexed { i, table ->
            masteredTable(table, TABLES_TEST_NOW + i.days)
        }

        val four = statusOf(reviews, MathFactsMilestone.FOUR_TABLES_MASTERED)
        val all = statusOf(reviews, MathFactsMilestone.ALL_TABLES_MASTERED)

        assertTrue(four.isAchieved)
        assertEquals(4, four.current)
        // Timestamped by the fourth table, not the most recent activity.
        assertEquals(TABLES_TEST_NOW + 3.days, four.achievedAt)

        assertFalse(all.isAchieved)
        assertEquals(4, all.current)
        assertEquals(MathFactsRoster.tables.size, all.target)
    }

    @Test
    fun `mastering every table clinches all four milestones`() {
        val reviews = MathFactsRoster.tables.flatMapIndexed { i, table ->
            masteredTable(table, TABLES_TEST_NOW + i.days)
        }

        val statuses = useCase(reviews)

        assertTrue(statuses.all { it.isAchieved })
        val all = statuses.single { it.milestone == MathFactsMilestone.ALL_TABLES_MASTERED }
        assertEquals(MathFactsRoster.tables.size, all.current)
    }

    @Test
    fun `re-drilling a mastered table never moves its achievement date`() {
        val earned = masteredTable(7, TABLES_TEST_NOW)
        // Box-4 cards fall due every 15 days, so this happens constantly.
        val reviewedAgain = earned.map { it.copy(updatedAt = TABLES_TEST_NOW + 30.days) }

        val before = statusOf(earned, MathFactsMilestone.FIRST_TABLE_MASTERED)
        val after = statusOf(reviewedAgain, MathFactsMilestone.FIRST_TABLE_MASTERED)

        // A parent ties a physical gift to this date; it must not wander.
        assertEquals(TABLES_TEST_NOW, before.achievedAt)
        assertEquals(before.achievedAt, after.achievedAt)
    }

    @Test
    fun `a later lapse does not un-earn a table the child already mastered`() {
        val earned = masteredTable(7, TABLES_TEST_NOW).toMutableList()
        // One fact slips back a box months later.
        val lapsed = earned.removeAt(0).copy(box = LeitnerSchedule.MAX_BOX - 1)
        earned.add(lapsed)

        val table = statusOf(earned, MathFactsMilestone.FIRST_TABLE_MASTERED)

        assertTrue(table.isAchieved)
        assertEquals(1, table.current)
        assertEquals(TABLES_TEST_NOW, table.achievedAt)
    }

    @Test
    fun `a fact that never topped out keeps its table unmastered`() {
        val reviews = masteredTable(7, TABLES_TEST_NOW).toMutableList()
        val neverMastered = reviews.removeAt(0).copy(box = 1, masteredAt = null)
        reviews.add(neverMastered)

        val table = statusOf(reviews, MathFactsMilestone.FIRST_TABLE_MASTERED)

        assertFalse(table.isAchieved)
    }

    @Test
    fun `duplicate rows for one fact cannot fake a mastered table`() {
        // The unique index should prevent this, but a milestone must not be
        // one bad migration away from handing out a gift that was not earned.
        val single = factReview(
            table = 7,
            multiplicand = 1,
            box = LeitnerSchedule.MAX_BOX,
            dueAt = TABLES_TEST_NOW,
        )
        val reviews = List(MathFactsRoster.factsOf(7).size) { single }

        val table = statusOf(reviews, MathFactsMilestone.FIRST_TABLE_MASTERED)

        assertFalse(table.isAchieved)
    }

    @Test
    fun `rows outside the roster never count towards a milestone`() {
        // A table 13 left behind by an older build must not fake progress.
        val reviews = (1..10).map { m ->
            factReview(13, m, box = LeitnerSchedule.MAX_BOX, dueAt = TABLES_TEST_NOW)
        }

        val statuses = useCase(reviews)

        assertTrue(statuses.none { it.isAchieved })
    }
}
