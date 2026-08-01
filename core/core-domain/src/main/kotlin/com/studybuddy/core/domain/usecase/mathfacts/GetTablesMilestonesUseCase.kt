package com.studybuddy.core.domain.usecase.mathfacts

import com.studybuddy.core.domain.model.mathfacts.MathFactReview
import com.studybuddy.core.domain.model.mathfacts.MathFactsMilestone
import com.studybuddy.core.domain.model.mathfacts.MathFactsMilestoneStatus
import com.studybuddy.core.domain.model.mathfacts.MathFactsRoster
import com.studybuddy.core.domain.model.srs.LeitnerMilestones
import javax.inject.Inject
import kotlinx.datetime.Instant

/**
 * Derives Jardin des Tables milestone statuses from a profile's review rows.
 *
 * Mastery is read from each card's latched `masteredAt` stamp rather than its
 * current box, so an achievement date never drifts as the card is re-drilled
 * and a later lapse never un-earns a milestone the child already reached.
 */
class GetTablesMilestonesUseCase @Inject constructor() {

    operator fun invoke(reviews: List<MathFactReview>): List<MathFactsMilestoneStatus> {
        val valid = reviews.filter { MathFactsRoster.isValid(it.fact) }
        val firstFactAt = valid.mapNotNull { it.masteredAt }.minOrNull()

        val tableTimes = LeitnerMilestones.groupCompletionTimes(
            cards = valid,
            keyOf = { it.fact },
            groupOf = { it.table },
            masteredAt = { it.masteredAt },
            sizeOfGroup = { table -> MathFactsRoster.factsOf(table).size },
        )

        return listOf(
            MathFactsMilestoneStatus(
                milestone = MathFactsMilestone.FIRST_FACT_MASTERED,
                current = if (firstFactAt != null) 1 else 0,
                target = 1,
                achievedAt = firstFactAt,
            ),
            tableMilestone(MathFactsMilestone.FIRST_TABLE_MASTERED, target = 1, times = tableTimes),
            tableMilestone(MathFactsMilestone.FOUR_TABLES_MASTERED, target = FOUR, times = tableTimes),
            tableMilestone(
                MathFactsMilestone.ALL_TABLES_MASTERED,
                target = MathFactsRoster.tables.size,
                times = tableTimes,
            ),
        )
    }

    private fun tableMilestone(
        milestone: MathFactsMilestone,
        target: Int,
        times: List<Instant>,
    ): MathFactsMilestoneStatus {
        val (current, achievedAt) = LeitnerMilestones.progressTowards(times, target)
        return MathFactsMilestoneStatus(
            milestone = milestone,
            current = current,
            target = target,
            achievedAt = achievedAt,
        )
    }

    private companion object {
        const val FOUR = 4
    }
}
