package com.studybuddy.core.domain.usecase.conjugation

import com.studybuddy.core.domain.model.conjugation.AtelierMilestone
import com.studybuddy.core.domain.model.conjugation.AtelierMilestoneStatus
import com.studybuddy.core.domain.model.conjugation.AtelierReview
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson
import com.studybuddy.core.domain.model.conjugation.ConjugationTense
import com.studybuddy.core.domain.model.conjugation.FrenchVerbs
import com.studybuddy.core.domain.model.srs.LeitnerMilestones
import javax.inject.Inject
import kotlinx.datetime.Instant

/**
 * Derives Atelier milestone statuses from a profile's review rows.
 *
 * Mastery is read from each card's latched `masteredAt` stamp rather than its
 * current box, so an achievement date never drifts as the card is re-drilled
 * and a later lapse never un-earns a milestone the child already reached.
 */
class GetAtelierMilestonesUseCase @Inject constructor() {

    operator fun invoke(reviews: List<AtelierReview>): List<AtelierMilestoneStatus> {
        val valid = reviews.filter { FrenchVerbs.byId(it.verbId) != null }
        val firstCardAt = valid.mapNotNull { it.masteredAt }.minOrNull()

        val verbTimes = LeitnerMilestones.groupCompletionTimes(
            cards = valid,
            keyOf = { Triple(it.verbId, it.tense, it.person) },
            groupOf = { it.verbId },
            masteredAt = { it.masteredAt },
            sizeOfGroup = { CARDS_PER_VERB },
        )

        return listOf(
            AtelierMilestoneStatus(
                milestone = AtelierMilestone.FIRST_CARD_MASTERED,
                current = if (firstCardAt != null) 1 else 0,
                target = 1,
                achievedAt = firstCardAt,
            ),
            verbMilestone(AtelierMilestone.FIRST_VERB_MASTERED, target = 1, times = verbTimes),
            verbMilestone(AtelierMilestone.FIVE_VERBS_MASTERED, target = FIVE, times = verbTimes),
            verbMilestone(
                AtelierMilestone.ALL_VERBS_MASTERED,
                target = FrenchVerbs.all.size,
                times = verbTimes,
            ),
        )
    }

    private fun verbMilestone(
        milestone: AtelierMilestone,
        target: Int,
        times: List<Instant>,
    ): AtelierMilestoneStatus {
        val (current, achievedAt) = LeitnerMilestones.progressTowards(times, target)
        return AtelierMilestoneStatus(
            milestone = milestone,
            current = current,
            target = target,
            achievedAt = achievedAt,
        )
    }

    private companion object {
        const val FIVE = 5

        /** Every (tense × person) card a single verb has. */
        val CARDS_PER_VERB = ConjugationTense.entries.size * ConjugationPerson.entries.size
    }
}
