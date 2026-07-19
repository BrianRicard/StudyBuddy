package com.studybuddy.core.data.repository

import com.studybuddy.core.data.db.dao.MathFactsReviewDao
import com.studybuddy.core.data.db.entity.MathFactReviewEntity
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
 * In-memory fake so the DAO's default [MathFactsReviewDao.recordAnswer] merge
 * logic runs for real instead of being mocked away.
 */
private class FakeMathFactsReviewDao : MathFactsReviewDao {
    private val rows = MutableStateFlow<Map<String, MathFactReviewEntity>>(emptyMap())

    private fun key(entity: MathFactReviewEntity) = "${entity.profileId}|${entity.tableNumber}|${entity.multiplicand}"

    override fun getReviewsForProfile(profileId: String): Flow<List<MathFactReviewEntity>> =
        rows.map { all -> all.values.filter { it.profileId == profileId } }

    override suspend fun getAllReviews(): List<MathFactReviewEntity> = rows.value.values.toList()

    override suspend fun getReview(
        profileId: String,
        tableNumber: Int,
        multiplicand: Int,
    ): MathFactReviewEntity? = rows.value["$profileId|$tableNumber|$multiplicand"]

    override suspend fun insert(review: MathFactReviewEntity) {
        check(key(review) !in rows.value) { "unique index violation" }
        rows.value += (key(review) to review)
    }

    override suspend fun update(review: MathFactReviewEntity) {
        check(key(review) in rows.value) { "update of missing row" }
        rows.value += (key(review) to review)
    }
}

class LocalMathFactsReviewRepositoryTest {

    private val dao = FakeMathFactsReviewDao()
    private val repository = LocalMathFactsReviewRepository(dao)

    private suspend fun answer(
        correct: Boolean,
        now: Instant = NOW,
        table: Int = 7,
        multiplicand: Int = 8,
    ) = repository.recordAnswer(
        profileId = PROFILE,
        table = table,
        multiplicand = multiplicand,
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
        assertEquals(7, outcome.review.table)
        assertEquals(8, outcome.review.multiplicand)
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
    }

    @Test
    fun `the same row is reused across answers and cards are keyed per fact`() = runTest {
        val first = answer(correct = true)
        val second = answer(correct = false, now = NOW + 1.days)
        answer(correct = true, table = 7, multiplicand = 9)
        answer(correct = true, table = 3, multiplicand = 8)

        assertEquals(first.review.id, second.review.id)
        assertEquals(3, repository.getReviews(PROFILE).first().size)
    }

    @Test
    fun `profiles are isolated`() = runTest {
        answer(correct = true)

        assertTrue(repository.getReviews("someone-else").first().isEmpty())
    }
}
