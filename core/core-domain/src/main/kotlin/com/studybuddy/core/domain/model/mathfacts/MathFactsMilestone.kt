package com.studybuddy.core.domain.model.mathfacts

import kotlinx.datetime.Instant

/**
 * Parent-facing Jardin des Tables milestones, shown on the Stats screen so
 * real-world rewards can be tied to verifiable memory progress.
 *
 * Stats-only achievements with no in-app unlock, mirroring
 * [com.studybuddy.core.domain.model.conjugation.AtelierMilestone]: the drill
 * already awards the points, and these are a progress lens for grown-ups.
 */
enum class MathFactsMilestone {
    FIRST_FACT_MASTERED,
    FIRST_TABLE_MASTERED,
    FOUR_TABLES_MASTERED,
    ALL_TABLES_MASTERED,
}

/**
 * @property current Progress toward [target] (e.g. 2 of 4 tables mastered).
 * @property achievedAt When the milestone was reached, null if not yet.
 */
data class MathFactsMilestoneStatus(
    val milestone: MathFactsMilestone,
    val current: Int,
    val target: Int,
    val achievedAt: Instant?,
) {
    val isAchieved: Boolean get() = achievedAt != null
}
