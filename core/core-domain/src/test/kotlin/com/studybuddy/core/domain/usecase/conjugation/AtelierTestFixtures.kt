package com.studybuddy.core.domain.usecase.conjugation

import com.studybuddy.core.domain.model.conjugation.AtelierReview
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson
import com.studybuddy.core.domain.model.conjugation.ConjugationTense
import com.studybuddy.core.domain.repository.AtelierAnswerOutcome
import com.studybuddy.core.domain.repository.AtelierReviewRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Instant

internal const val ATELIER_TEST_PROFILE = "profile-1"
internal val ATELIER_TEST_NOW = Instant.fromEpochMilliseconds(1_750_000_000_000)

/** Read-only fake for use cases that only consume the review flow. */
internal class FakeAtelierReviewRepository(
    var reviews: List<AtelierReview> = emptyList(),
) : AtelierReviewRepository {

    override fun getReviews(profileId: String) = flowOf(reviews.filter { it.profileId == profileId })

    override suspend fun recordAnswer(
        profileId: String,
        verbId: String,
        tense: ConjugationTense,
        person: ConjugationPerson,
        correct: Boolean,
        now: Instant,
    ): AtelierAnswerOutcome = error("not used in these tests")

    override suspend fun sync() = Unit
}

internal fun atelierReview(
    verbId: String,
    tense: ConjugationTense = ConjugationTense.PRESENT,
    person: ConjugationPerson = ConjugationPerson.JE,
    box: Int = 2,
    dueAt: Instant = ATELIER_TEST_NOW,
    profileId: String = ATELIER_TEST_PROFILE,
) = AtelierReview(
    id = "$verbId-$tense-$person",
    profileId = profileId,
    verbId = verbId,
    tense = tense,
    person = person,
    box = box,
    dueAt = dueAt,
    lapses = 0,
    updatedAt = dueAt,
)
