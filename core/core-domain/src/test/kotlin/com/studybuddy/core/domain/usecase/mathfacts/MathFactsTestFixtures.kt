package com.studybuddy.core.domain.usecase.mathfacts

import com.studybuddy.core.domain.model.mathfacts.MathFactReview
import com.studybuddy.core.domain.model.srs.LeitnerSchedule
import com.studybuddy.core.domain.repository.MathFactAnswerOutcome
import com.studybuddy.core.domain.repository.MathFactsReviewRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

internal const val TABLES_TEST_PROFILE = "profile-1"
internal val TABLES_TEST_NOW = Instant.fromEpochMilliseconds(1_750_000_000_000)

/** A clock pinned to [TABLES_TEST_NOW] so "is it due?" never depends on wall time. */
internal class FixedClock(var now: Instant = TABLES_TEST_NOW) : Clock {
    override fun now(): Instant = now
}

/** Read-only fake for use cases that only consume the review flow. */
internal class FakeMathFactsReviewRepository(
    var reviews: List<MathFactReview> = emptyList(),
) : MathFactsReviewRepository {

    override fun getReviews(profileId: String) = flowOf(reviews.filter { it.profileId == profileId })

    override suspend fun recordAnswer(
        profileId: String,
        table: Int,
        multiplicand: Int,
        correct: Boolean,
        now: Instant,
    ): MathFactAnswerOutcome = error("not used in these tests")

    override suspend fun sync() = Unit
}

/**
 * Mirrors what the repository actually writes: a top-box card is latched at
 * its update time. Pass [masteredAt] explicitly to model a card that has been
 * re-drilled since (updatedAt moves, masteredAt must not).
 */
internal fun factReview(
    table: Int,
    multiplicand: Int,
    box: Int = 2,
    dueAt: Instant = TABLES_TEST_NOW,
    updatedAt: Instant = dueAt,
    masteredAt: Instant? = updatedAt.takeIf { box >= LeitnerSchedule.MAX_BOX },
    profileId: String = TABLES_TEST_PROFILE,
) = MathFactReview(
    id = "$table-x-$multiplicand",
    profileId = profileId,
    table = table,
    multiplicand = multiplicand,
    box = box,
    dueAt = dueAt,
    lapses = 0,
    updatedAt = updatedAt,
    masteredAt = masteredAt,
)
