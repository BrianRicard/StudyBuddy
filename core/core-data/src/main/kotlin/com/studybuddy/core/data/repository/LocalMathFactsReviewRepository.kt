package com.studybuddy.core.data.repository

import com.studybuddy.core.data.db.dao.MathFactsReviewDao
import com.studybuddy.core.data.db.entity.MathFactReviewEntity
import com.studybuddy.core.data.mapper.toDomain
import com.studybuddy.core.domain.model.mathfacts.MathFactReview
import com.studybuddy.core.domain.model.srs.LeitnerSchedule
import com.studybuddy.core.domain.repository.MathFactAnswerOutcome
import com.studybuddy.core.domain.repository.MathFactsReviewRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

@Singleton
class LocalMathFactsReviewRepository @Inject constructor(
    private val dao: MathFactsReviewDao,
) : MathFactsReviewRepository {

    override fun getReviews(profileId: String): Flow<List<MathFactReview>> =
        dao.getReviewsForProfile(profileId).map { rows -> rows.map { it.toDomain() } }

    override suspend fun recordAnswer(
        profileId: String,
        table: Int,
        multiplicand: Int,
        correct: Boolean,
        now: Instant,
    ): MathFactAnswerOutcome {
        val nowMillis = now.toEpochMilliseconds()
        var previousBox: Int? = null
        val merged = dao.recordAnswer(profileId, table, multiplicand) { existing ->
            previousBox = existing?.box
            val outcome = LeitnerSchedule.answered(box = existing?.box ?: 0, correct = correct)
            MathFactReviewEntity(
                id = existing?.id ?: UUID.randomUUID().toString(),
                profileId = profileId,
                tableNumber = table,
                multiplicand = multiplicand,
                box = outcome.box,
                dueAt = nowMillis + outcome.nextDelay.inWholeMilliseconds,
                lapses = (existing?.lapses ?: 0) + if (correct) 0 else 1,
                updatedAt = nowMillis,
            )
        }
        return MathFactAnswerOutcome(previousBox = previousBox, review = merged.toDomain())
    }

    override suspend fun sync() { /* no-op: cloud migration hook */ }
}
