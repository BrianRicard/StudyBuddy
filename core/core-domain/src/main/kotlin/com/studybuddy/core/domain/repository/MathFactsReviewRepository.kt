package com.studybuddy.core.domain.repository

import com.studybuddy.core.domain.model.mathfacts.MathFactReview
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
data class MathFactAnswerOutcome(
    val previousBox: Int?,
    val review: MathFactReview,
)

interface MathFactsReviewRepository {

    fun getReviews(profileId: String): Flow<List<MathFactReview>>

    /**
     * Records one drill answer for a fact and reschedules it per
     * [com.studybuddy.core.domain.model.srs.LeitnerSchedule].
     *
     * The card row is created on first sighting. Every answer updates the
     * schedule, whichever mode (Révision, Explorer, Surprise) produced it, so
     * free exploration feeds the review plan too.
     */
    suspend fun recordAnswer(
        profileId: String,
        table: Int,
        multiplicand: Int,
        correct: Boolean,
        now: Instant,
    ): MathFactAnswerOutcome

    suspend fun sync() // Cloud migration hook
}
