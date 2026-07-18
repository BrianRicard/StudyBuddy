package com.studybuddy.core.domain.usecase.conjugation

import com.studybuddy.core.domain.model.conjugation.AtelierGarden
import com.studybuddy.core.domain.model.conjugation.AtelierGrowth
import com.studybuddy.core.domain.model.conjugation.AtelierReview
import com.studybuddy.core.domain.model.conjugation.AtelierVerbGarden
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson
import com.studybuddy.core.domain.model.conjugation.ConjugationTense
import com.studybuddy.core.domain.model.conjugation.FrenchVerbs
import com.studybuddy.core.domain.repository.AtelierReviewRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

/**
 * Emits the Atelier garden: per verb × tense growth stages plus the due
 * counts that drive the Révision button and the home-screen nudge.
 */
class GetAtelierGardenUseCase @Inject constructor(
    private val repository: AtelierReviewRepository,
) {

    operator fun invoke(
        profileId: String,
        now: Instant,
    ): Flow<AtelierGarden> = repository.getReviews(profileId).map { reviews -> buildGarden(reviews, now) }

    private fun buildGarden(
        reviews: List<AtelierReview>,
        now: Instant,
    ): AtelierGarden {
        val byCard = reviews.associateBy { Triple(it.verbId, it.tense, it.person) }
        val due = reviews.filter { it.dueAt <= now && FrenchVerbs.byId(it.verbId) != null }

        val verbs = FrenchVerbs.all.map { verb ->
            AtelierVerbGarden(
                verb = verb,
                growth = ConjugationTense.entries.associateWith { tense ->
                    AtelierGrowth.fromBoxes(
                        ConjugationPerson.entries.map { person ->
                            byCard[Triple(verb.id, tense, person)]?.box ?: 0
                        },
                    )
                },
            )
        }

        return AtelierGarden(
            dueCardCount = due.size,
            dueVerbCount = due.distinctBy { it.verbId }.size,
            verbs = verbs,
        )
    }
}
