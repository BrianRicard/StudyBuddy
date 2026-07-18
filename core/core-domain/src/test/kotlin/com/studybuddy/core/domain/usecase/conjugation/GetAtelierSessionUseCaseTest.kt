package com.studybuddy.core.domain.usecase.conjugation

import com.studybuddy.core.domain.model.conjugation.ConjugationPerson
import com.studybuddy.core.domain.model.conjugation.ConjugationTense
import com.studybuddy.core.domain.model.conjugation.FrenchVerbs
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GetAtelierSessionUseCaseTest {

    private val repository = FakeAtelierReviewRepository()
    private val useCase = GetAtelierSessionUseCase(repository)

    @Test
    fun `a brand-new profile gets present-tense cards in introduction order`() = runTest {
        val session = useCase(ATELIER_TEST_PROFILE, ATELIER_TEST_NOW)

        assertEquals(GetAtelierSessionUseCase.SESSION_SIZE, session.size)
        assertTrue(session.all { it.isNew && it.tense == ConjugationTense.PRESENT })
        // être walks all six persons, then avoir starts.
        assertEquals(List(6) { "etre" } + List(4) { "avoir" }, session.map { it.verb.id })
        assertEquals(ConjugationPerson.entries.toList(), session.take(6).map { it.person })
    }

    @Test
    fun `due cards come first, most overdue first`() = runTest {
        repository.reviews = listOf(
            atelierReview("aimer", dueAt = ATELIER_TEST_NOW - 1.days),
            atelierReview("aller", dueAt = ATELIER_TEST_NOW - 3.days),
            atelierReview("faire", dueAt = ATELIER_TEST_NOW - 2.days),
        )

        val session = useCase(ATELIER_TEST_PROFILE, ATELIER_TEST_NOW)

        assertEquals(listOf("aller", "faire", "aimer"), session.take(3).map { it.verb.id })
        assertTrue(session.take(3).none { it.isNew })
        // The rest of the session is filled with new cards.
        assertTrue(session.drop(3).all { it.isNew })
        assertEquals(GetAtelierSessionUseCase.SESSION_SIZE, session.size)
    }

    @Test
    fun `a card due exactly now counts as due`() = runTest {
        repository.reviews = listOf(atelierReview("dire", dueAt = ATELIER_TEST_NOW))

        val session = useCase(ATELIER_TEST_PROFILE, ATELIER_TEST_NOW)

        assertEquals("dire", session.first().verb.id)
        assertTrue(!session.first().isNew)
    }

    @Test
    fun `reviewed cards are not reintroduced as new`() = runTest {
        repository.reviews = listOf(
            atelierReview("etre", person = ConjugationPerson.JE, dueAt = ATELIER_TEST_NOW + 5.days),
        )

        val session = useCase(ATELIER_TEST_PROFILE, ATELIER_TEST_NOW)

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
                    atelierReview(verb.id, tense, person, dueAt = ATELIER_TEST_NOW + offsetHours.hours)
                }
            }
        }

        val session = useCase(ATELIER_TEST_PROFILE, ATELIER_TEST_NOW)

        assertEquals(GetAtelierSessionUseCase.SESSION_SIZE, session.size)
        assertTrue(session.none { it.isNew })
        // Soonest due first: the first ten cards created above.
        assertEquals(List(6) { "etre" } + List(4) { "avoir" }, session.map { it.verb.id })
    }

    @Test
    fun `session size is respected`() = runTest {
        assertEquals(3, useCase(ATELIER_TEST_PROFILE, ATELIER_TEST_NOW, size = 3).size)
    }

    @Test
    fun `rows for unknown verbs are ignored`() = runTest {
        repository.reviews = listOf(atelierReview("kangourou", dueAt = ATELIER_TEST_NOW - 1.days))

        val session = useCase(ATELIER_TEST_PROFILE, ATELIER_TEST_NOW)

        assertTrue(session.none { it.verb.id == "kangourou" })
        assertEquals(GetAtelierSessionUseCase.SESSION_SIZE, session.size)
    }

    @Test
    fun `another profile's reviews are invisible`() = runTest {
        repository.reviews = listOf(
            atelierReview("aimer", dueAt = ATELIER_TEST_NOW - 1.days, profileId = "someone-else"),
        )

        val session = useCase(ATELIER_TEST_PROFILE, ATELIER_TEST_NOW)

        assertTrue(session.all { it.isNew })
    }
}
