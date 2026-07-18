package com.studybuddy.core.domain.usecase.conjugation

import com.studybuddy.core.domain.model.conjugation.AtelierReview
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson
import com.studybuddy.core.domain.model.conjugation.ConjugationTense
import com.studybuddy.core.domain.model.conjugation.FrenchVerbs
import com.studybuddy.core.domain.repository.AtelierAnswerOutcome
import com.studybuddy.core.domain.repository.AtelierReviewRepository
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private const val PROFILE = "profile-1"
private val NOW = Instant.fromEpochMilliseconds(1_750_000_000_000)

private class FakeAtelierReviewRepository(
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

private fun review(
    verbId: String,
    tense: ConjugationTense = ConjugationTense.PRESENT,
    person: ConjugationPerson = ConjugationPerson.JE,
    box: Int = 2,
    dueAt: Instant,
    profileId: String = PROFILE,
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

class GetAtelierSessionUseCaseTest {

    private val repository = FakeAtelierReviewRepository()
    private val useCase = GetAtelierSessionUseCase(repository)

    @Test
    fun `a brand-new profile gets present-tense cards in introduction order`() = runTest {
        val session = useCase(PROFILE, NOW)

        assertEquals(GetAtelierSessionUseCase.SESSION_SIZE, session.size)
        assertTrue(session.all { it.isNew && it.tense == ConjugationTense.PRESENT })
        // être walks all six persons, then avoir starts.
        assertEquals(List(6) { "etre" } + List(4) { "avoir" }, session.map { it.verb.id })
        assertEquals(ConjugationPerson.entries.toList(), session.take(6).map { it.person })
    }

    @Test
    fun `due cards come first, most overdue first`() = runTest {
        repository.reviews = listOf(
            review("aimer", dueAt = NOW - 1.days),
            review("aller", dueAt = NOW - 3.days),
            review("faire", dueAt = NOW - 2.days),
        )

        val session = useCase(PROFILE, NOW)

        assertEquals(listOf("aller", "faire", "aimer"), session.take(3).map { it.verb.id })
        assertTrue(session.take(3).none { it.isNew })
        // The rest of the session is filled with new cards.
        assertTrue(session.drop(3).all { it.isNew })
        assertEquals(GetAtelierSessionUseCase.SESSION_SIZE, session.size)
    }

    @Test
    fun `a card due exactly now counts as due`() = runTest {
        repository.reviews = listOf(review("dire", dueAt = NOW))

        val session = useCase(PROFILE, NOW)

        assertEquals("dire", session.first().verb.id)
        assertTrue(!session.first().isNew)
    }

    @Test
    fun `reviewed cards are not reintroduced as new`() = runTest {
        repository.reviews = listOf(
            review("etre", person = ConjugationPerson.JE, dueAt = NOW + 5.days),
        )

        val session = useCase(PROFILE, NOW)

        val etreJePresent = session.filter {
            it.verb.id == "etre" &&
                it.person == ConjugationPerson.JE &&
                it.tense == ConjugationTense.PRESENT
        }
        assertTrue(etreJePresent.none { it.isNew }, "reviewed card must not come back as new")
    }

    @Test
    fun `when everything is reviewed, upcoming cards fill the session soonest first`() = runTest {
        var offsetHours = 0
        repository.reviews = ConjugationTense.entries.flatMap { tense ->
            FrenchVerbs.all.flatMap { verb ->
                ConjugationPerson.entries.map { person ->
                    offsetHours += 1
                    review(verb.id, tense, person, dueAt = NOW + offsetHours.hours)
                }
            }
        }

        val session = useCase(PROFILE, NOW)

        assertEquals(GetAtelierSessionUseCase.SESSION_SIZE, session.size)
        assertTrue(session.none { it.isNew })
        // Soonest due first: the first ten cards created above.
        assertEquals(List(6) { "etre" } + List(4) { "avoir" }, session.map { it.verb.id })
    }

    @Test
    fun `session size is respected`() = runTest {
        assertEquals(3, useCase(PROFILE, NOW, size = 3).size)
    }

    @Test
    fun `rows for unknown verbs are ignored`() = runTest {
        repository.reviews = listOf(review("kangourou", dueAt = NOW - 1.days))

        val session = useCase(PROFILE, NOW)

        assertTrue(session.none { it.verb.id == "kangourou" })
        assertEquals(GetAtelierSessionUseCase.SESSION_SIZE, session.size)
    }

    @Test
    fun `another profile's reviews are invisible`() = runTest {
        repository.reviews = listOf(
            review("aimer", dueAt = NOW - 1.days, profileId = "someone-else"),
        )

        val session = useCase(PROFILE, NOW)

        assertTrue(session.all { it.isNew })
    }
}
