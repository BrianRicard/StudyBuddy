package com.studybuddy.core.domain.model.conjugation

import kotlinx.datetime.Instant

/**
 * Parent-facing quest milestones, shown on the Stats screen so real-world
 * rewards can be tied to verifiable progress.
 */
enum class ConjugationMilestone {
    FIRST_STEP,
    FIRST_VERB,
    THREE_VERBS,
    ALL_VERBS,
    PERFECT_QUEST,
}

/**
 * @property current Progress toward [target] (e.g. 2 of 3 verbs mastered).
 * @property achievedAt When the milestone was reached, null if not yet.
 */
data class MilestoneStatus(
    val milestone: ConjugationMilestone,
    val current: Int,
    val target: Int,
    val achievedAt: Instant?,
) {
    val isAchieved: Boolean get() = achievedAt != null
}
