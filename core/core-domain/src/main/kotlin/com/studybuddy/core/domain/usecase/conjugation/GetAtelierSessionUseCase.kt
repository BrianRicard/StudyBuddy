package com.studybuddy.core.domain.usecase.conjugation

import com.studybuddy.core.domain.model.conjugation.AtelierCard
import com.studybuddy.core.domain.model.conjugation.AtelierReview
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson
import com.studybuddy.core.domain.model.conjugation.ConjugationTense
import com.studybuddy.core.domain.model.conjugation.FrenchVerbs
import com.studybuddy.core.domain.repository.AtelierReviewRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant

/**
 * Builds a Révision drill session:
 *
 * 1. Due cards first, most overdue first — repetition beats novelty.
 * 2. Then new (never-seen) cards in introduction order: tense-major
 *    (all of présent before any futur, futur before imparfait), verbs in
 *    roster order, persons je→ils. So the child broadly masters présent
 *    before the review plan introduces new tenses.
 * 3. If the session still has room, upcoming cards (soonest due first) fill
 *    it — reviewing a little early is harmless and the session should always
 *    feel full.
 */
class GetAtelierSessionUseCase @Inject constructor(
    private val repository: AtelierReviewRepository,
) {

    suspend operator fun invoke(
        profileId: String,
        now: Instant,
        size: Int = SESSION_SIZE,
    ): List<AtelierCard> {
        val reviews = repository.getReviews(profileId).first()
            .filter { FrenchVerbs.byId(it.verbId) != null }
            .associateBy { Triple(it.verbId, it.tense, it.person) }

        val due = reviews.values
            .filter { it.dueAt <= now }
            .sortedBy { it.dueAt }
            .mapNotNull { it.toCard() }

        val new = introductionOrder()
            .filterNot { Triple(it.verb.id, it.tense, it.person) in reviews }

        val upcoming = reviews.values
            .filter { it.dueAt > now }
            .sortedBy { it.dueAt }
            .mapNotNull { it.toCard() }

        return (due + new + upcoming).take(size)
    }

    /** Every card in the Atelier, in the order new cards are introduced. */
    private fun introductionOrder(): List<AtelierCard> = ConjugationTense.entries.flatMap { tense ->
        FrenchVerbs.all.flatMap { verb ->
            ConjugationPerson.entries.map { person ->
                AtelierCard(verb = verb, tense = tense, person = person, box = 0, isNew = true)
            }
        }
    }

    private fun AtelierReview.toCard(): AtelierCard? {
        val verb = FrenchVerbs.byId(verbId) ?: return null
        return AtelierCard(verb = verb, tense = tense, person = person, box = box, isNew = false)
    }

    companion object {
        const val SESSION_SIZE = 10
    }
}
