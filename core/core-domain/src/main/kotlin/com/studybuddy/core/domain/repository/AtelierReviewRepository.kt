package com.studybuddy.core.domain.repository

import com.studybuddy.core.domain.model.conjugation.AtelierReview
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson
import com.studybuddy.core.domain.model.conjugation.ConjugationTense
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

/**
 * Result of recording one drill answer, so callers can animate growth
 * (box changes) without re-deriving state from the flow.
 *
 * @property previousBox The card's box before this answer, or null when this
 * was the card's very first sighting.
 * @property review The card's stored state after the answer.
 */
data class AtelierAnswerOutcome(
    val previousBox: Int?,
    val review: AtelierReview,
)

interface AtelierReviewRepository {

    fun getReviews(profileId: String): Flow<List<AtelierReview>>

    /**
     * Records one drill answer for a card and reschedules it per
     * [com.studybuddy.core.domain.model.conjugation.AtelierSchedule].
     *
     * The card row is created on first sighting. Every answer updates the
     * schedule, whichever mode (Révision, Explorer, Surprise) produced it, so
     * free exploration feeds the review plan too.
     */
    suspend fun recordAnswer(
        profileId: String,
        verbId: String,
        tense: ConjugationTense,
        person: ConjugationPerson,
        correct: Boolean,
        now: Instant,
    ): AtelierAnswerOutcome

    suspend fun sync() // Cloud migration hook
}
