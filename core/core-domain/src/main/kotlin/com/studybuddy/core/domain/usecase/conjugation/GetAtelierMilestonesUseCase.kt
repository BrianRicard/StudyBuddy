package com.studybuddy.core.domain.usecase.conjugation

import com.studybuddy.core.domain.model.conjugation.AtelierMilestone
import com.studybuddy.core.domain.model.conjugation.AtelierMilestoneStatus
import com.studybuddy.core.domain.model.conjugation.AtelierReview
import com.studybuddy.core.domain.model.conjugation.AtelierSchedule
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson
import com.studybuddy.core.domain.model.conjugation.ConjugationTense
import com.studybuddy.core.domain.model.conjugation.FrenchVerbs
import javax.inject.Inject
import kotlinx.datetime.Instant

/**
 * Derives Atelier milestone statuses from a profile's review rows.
 *
 * A card is "mastered" once it reaches the top Leitner box; a verb is mastered
 * once every one of its cards (all tenses × all persons) is at the top box.
 * Achievement timestamps come from the last update that clinched the milestone,
 * so parents can see roughly when each goal was reached.
 */
class GetAtelierMilestonesUseCase @Inject constructor() {

    operator fun invoke(reviews: List<AtelierReview>): List<AtelierMilestoneStatus> {
        val maxBoxCards = reviews.filter {
            it.box >= AtelierSchedule.MAX_BOX && FrenchVerbs.byId(it.verbId) != null
        }
        val firstCardAt = maxBoxCards.minByOrNull { it.updatedAt }?.updatedAt

        // A verb is fully mastered when all of its cards are at the top box.
        // The row key (verbId, tense, person) is unique, so counting top-box
        // cards per verb is enough — it can reach [CARDS_PER_VERB] only when
        // every card is mastered.
        val verbMasteredTimes = maxBoxCards
            .groupBy { it.verbId }
            .filterValues { it.size >= CARDS_PER_VERB }
            .values
            .map { cards -> cards.maxOf { it.updatedAt } }
            .sorted()

        return listOf(
            AtelierMilestoneStatus(
                milestone = AtelierMilestone.FIRST_CARD_MASTERED,
                current = if (maxBoxCards.isNotEmpty()) 1 else 0,
                target = 1,
                achievedAt = firstCardAt,
            ),
            verbMilestone(AtelierMilestone.FIRST_VERB_MASTERED, target = 1, times = verbMasteredTimes),
            verbMilestone(AtelierMilestone.FIVE_VERBS_MASTERED, target = FIVE, times = verbMasteredTimes),
            verbMilestone(
                AtelierMilestone.ALL_VERBS_MASTERED,
                target = FrenchVerbs.all.size,
                times = verbMasteredTimes,
            ),
        )
    }

    private fun verbMilestone(
        milestone: AtelierMilestone,
        target: Int,
        times: List<Instant>,
    ) = AtelierMilestoneStatus(
        milestone = milestone,
        current = times.size.coerceAtMost(target),
        target = target,
        achievedAt = times.getOrNull(target - 1),
    )

    private companion object {
        const val FIVE = 5

        /** Every (tense × person) card a single verb has. */
        val CARDS_PER_VERB = ConjugationTense.entries.size * ConjugationPerson.entries.size
    }
}
