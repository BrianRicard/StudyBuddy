package com.studybuddy.core.data.repository

import com.studybuddy.core.data.db.dao.AtelierReviewDao
import com.studybuddy.core.data.db.entity.AtelierReviewEntity
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson
import com.studybuddy.core.domain.model.conjugation.ConjugationTense
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private val NOW = Instant.fromEpochMilliseconds(1_750_000_000_000)
private const val PROFILE = "profile-1"

/**
 * In-memory fake so the DAO's default [AtelierReviewDao.recordAnswer] merge
 * logic runs for real instead of being mocked away.
 */
private class FakeAtelierReviewDao : AtelierReviewDao {
    private val rows = MutableStateFlow<Map<String, AtelierReviewEntity>>(emptyMap())

    private fun key(entity: AtelierReviewEntity) =
        "${entity.profileId}|${entity.verbId}|${entity.tense}|${entity.person}"

    override fun getReviewsForProfile(profileId: String): Flow<List<AtelierReviewEntity>> =
        rows.map { all -> all.values.filter { it.profileId == profileId } }

    override suspend fun getAllReviews(): List<AtelierReviewEntity> = rows.value.values.toList()

    override suspend fun getReview(
        profileId: String,
        verbId: String,
        tense: String,
        person: String,
    ): AtelierReviewEntity? = rows.value["$profileId|$verbId|$tense|$person"]

    override suspend fun insert(review: AtelierReviewEntity) {
        check(key(review) !in rows.value) { "unique index violation" }
        rows.value += (key(review) to review)
    }

    override suspend fun update(review: AtelierReviewEntity) {
        check(key(review) in rows.value) { "update of missing row" }
        rows.value += (key(review) to review)
    }
}

class LocalAtelierReviewRepositoryTest {

    private val dao = FakeAtelierReviewDao()
    private val repository = LocalAtelierReviewRepository(dao)

    private suspend fun answer(
        correct: Boolean,
        now: Instant = NOW,
        verbId: String = "etre",
    ) = repository.recordAnswer(
        profileId = PROFILE,
        verbId = verbId,
        tense = ConjugationTense.PRESENT,
        person = ConjugationPerson.JE,
        correct = correct,
        now = now,
    )

    @Test
    fun `first correct answer creates the card in box 1, due tomorrow`() = runTest {
        val outcome = answer(correct = true)

        assertNull(outcome.previousBox)
        assertEquals(1, outcome.review.box)
        assertEquals(NOW + 1.days, outcome.review.dueAt)
        assertEquals(0, outcome.review.lapses)
        assertEquals(NOW, outcome.review.updatedAt)
    }

    @Test
    fun `first wrong answer creates the card in box 0 with one lapse, due tomorrow`() = runTest {
        val outcome = answer(correct = false)

        assertNull(outcome.previousBox)
        assertEquals(0, outcome.review.box)
        assertEquals(NOW + 1.days, outcome.review.dueAt)
        assertEquals(1, outcome.review.lapses)
    }

    @Test
    fun `correct answers walk up the boxes with growing intervals`() = runTest {
        val expected = listOf(1 to 1, 2 to 3, 3 to 7, 4 to 15, 4 to 15)

        expected.forEachIndexed { index, (box, intervalDays) ->
            val now = NOW + index.days
            val outcome = answer(correct = true, now = now)
            assertEquals(box, outcome.review.box, "after answer ${index + 1}")
            assertEquals(now + intervalDays.days, outcome.review.dueAt, "after answer ${index + 1}")
        }
    }

    @Test
    fun `a wrong answer demotes one box, counts the lapse, and comes back tomorrow`() = runTest {
        repeat(3) { answer(correct = true, now = NOW + it.days) }

        val later = NOW + 10.days
        val outcome = answer(correct = false, now = later)

        assertEquals(3, outcome.previousBox)
        assertEquals(2, outcome.review.box)
        assertEquals(1, outcome.review.lapses)
        assertEquals(later + 1.days, outcome.review.dueAt)
        assertEquals(later, outcome.review.updatedAt)
    }

    @Test
    fun `the same row is reused across answers`() = runTest {
        val first = answer(correct = true)
        val second = answer(correct = false, now = NOW + 1.days)

        assertEquals(first.review.id, second.review.id)
        assertEquals(1, dao.getReviewsForProfile(PROFILE).first().size)
    }

    @Test
    fun `getReviews maps rows and drops unknown tense or person names`() = runTest {
        answer(correct = true)
        dao.insert(
            AtelierReviewEntity(
                id = "future-row",
                profileId = PROFILE,
                verbId = "etre",
                tense = "PLUS_QUE_PARFAIT",
                person = "JE",
                box = 1,
                dueAt = 0L,
                lapses = 0,
                updatedAt = 0L,
            ),
        )

        val reviews = repository.getReviews(PROFILE).first()

        assertEquals(1, reviews.size)
        assertEquals(ConjugationTense.PRESENT, reviews.single().tense)
    }

    @Test
    fun `cards are keyed per tense and person`() = runTest {
        answer(correct = true)
        repository.recordAnswer(
            profileId = PROFILE,
            verbId = "etre",
            tense = ConjugationTense.FUTUR,
            person = ConjugationPerson.JE,
            correct = true,
            now = NOW,
        )
        repository.recordAnswer(
            profileId = PROFILE,
            verbId = "etre",
            tense = ConjugationTense.PRESENT,
            person = ConjugationPerson.NOUS,
            correct = true,
            now = NOW,
        )

        assertEquals(3, repository.getReviews(PROFILE).first().size)
    }

    @Test
    fun `profiles are isolated`() = runTest {
        answer(correct = true)

        assertTrue(repository.getReviews("someone-else").first().isEmpty())
    }
}
