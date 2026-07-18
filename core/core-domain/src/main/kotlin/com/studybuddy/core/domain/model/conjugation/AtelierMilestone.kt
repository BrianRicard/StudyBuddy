package com.studybuddy.core.domain.model.conjugation

import kotlinx.datetime.Instant

/**
 * Parent-facing Atelier des Verbes milestones, shown on the Stats screen so
 * real-world rewards can be tied to verifiable memory progress.
 *
 * These are stats-only achievements (no in-app unlock), mirroring the Verb
 * Quest [ConjugationMilestone] pattern — the drill itself already awards the
 * points; milestones are a progress lens for grown-ups.
 */
enum class AtelierMilestone {
    FIRST_CARD_MASTERED,
    FIRST_VERB_MASTERED,
    FIVE_VERBS_MASTERED,
    ALL_VERBS_MASTERED,
}

/**
 * @property current Progress toward [target] (e.g. 2 of 5 verbs mastered).
 * @property achievedAt When the milestone was reached, null if not yet.
 */
data class AtelierMilestoneStatus(
    val milestone: AtelierMilestone,
    val current: Int,
    val target: Int,
    val achievedAt: Instant?,
) {
    val isAchieved: Boolean get() = achievedAt != null
}
