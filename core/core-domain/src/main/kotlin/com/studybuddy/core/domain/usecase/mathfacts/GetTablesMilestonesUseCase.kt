package com.studybuddy.core.domain.usecase.mathfacts

import com.studybuddy.core.domain.model.mathfacts.MathFactReview
import com.studybuddy.core.domain.model.mathfacts.MathFactsMilestone
import com.studybuddy.core.domain.model.mathfacts.MathFactsMilestoneStatus
import com.studybuddy.core.domain.model.mathfacts.MathFactsRoster
import com.studybuddy.core.domain.model.srs.LeitnerSchedule
import javax.inject.Inject
import kotlinx.datetime.Instant

/**
 * Derives Jardin des Tables milestone statuses from a profile's review rows.
 *
 * A fact is "mastered" once it reaches the top Leitner box; a table is mastered
 * once all ten of its facts are. Achievement timestamps come from the update
 * that clinched the milestone, so parents can see roughly when it was reached.
 */
class GetTablesMilestonesUseCase @Inject constructor() {

    operator fun invoke(reviews: List<MathFactReview>): List<MathFactsMilestoneStatus> {
        val mastered = reviews.filter {
            it.box >= LeitnerSchedule.MAX_BOX && MathFactsRoster.isValid(it.fact)
        }
        val firstFactAt = mastered.minByOrNull { it.updatedAt }?.updatedAt

        // The row key (table, multiplicand) is unique, so a table can only reach
        // FACTS_PER_TABLE top-box rows when every one of its facts is mastered.
        val tableMasteredTimes = mastered
            .groupBy { it.table }
            .filterValues { it.size >= FACTS_PER_TABLE }
            .values
            .map { facts -> facts.maxOf { it.updatedAt } }
            .sorted()

        return listOf(
            MathFactsMilestoneStatus(
                milestone = MathFactsMilestone.FIRST_FACT_MASTERED,
                current = if (mastered.isNotEmpty()) 1 else 0,
                target = 1,
                achievedAt = firstFactAt,
            ),
            tableMilestone(MathFactsMilestone.FIRST_TABLE_MASTERED, target = 1, times = tableMasteredTimes),
            tableMilestone(MathFactsMilestone.FOUR_TABLES_MASTERED, target = FOUR, times = tableMasteredTimes),
            tableMilestone(
                MathFactsMilestone.ALL_TABLES_MASTERED,
                target = MathFactsRoster.tables.size,
                times = tableMasteredTimes,
            ),
        )
    }

    private fun tableMilestone(
        milestone: MathFactsMilestone,
        target: Int,
        times: List<Instant>,
    ) = MathFactsMilestoneStatus(
        milestone = milestone,
        current = times.size.coerceAtMost(target),
        target = target,
        achievedAt = times.getOrNull(target - 1),
    )

    private companion object {
        const val FOUR = 4

        /** Every ×1–×10 fact a single table has. */
        val FACTS_PER_TABLE = MathFactsRoster.LAST_MULTIPLICAND - MathFactsRoster.FIRST_MULTIPLICAND + 1
    }
}
