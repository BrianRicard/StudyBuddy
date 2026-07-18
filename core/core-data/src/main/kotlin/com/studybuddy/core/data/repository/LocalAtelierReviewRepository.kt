package com.studybuddy.core.data.repository

import com.studybuddy.core.data.db.dao.AtelierReviewDao
import com.studybuddy.core.data.db.entity.AtelierReviewEntity
import com.studybuddy.core.data.mapper.toDomainOrNull
import com.studybuddy.core.domain.model.conjugation.AtelierReview
import com.studybuddy.core.domain.model.conjugation.AtelierSchedule
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson
import com.studybuddy.core.domain.model.conjugation.ConjugationTense
import com.studybuddy.core.domain.repository.AtelierAnswerOutcome
import com.studybuddy.core.domain.repository.AtelierReviewRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

@Singleton
class LocalAtelierReviewRepository @Inject constructor(
    private val dao: AtelierReviewDao,
) : AtelierReviewRepository {

    override fun getReviews(profileId: String): Flow<List<AtelierReview>> =
        dao.getReviewsForProfile(profileId).map { rows -> rows.mapNotNull { it.toDomainOrNull() } }

    override suspend fun recordAnswer(
        profileId: String,
        verbId: String,
        tense: ConjugationTense,
        person: ConjugationPerson,
        correct: Boolean,
        now: Instant,
    ): AtelierAnswerOutcome {
        val nowMillis = now.toEpochMilliseconds()
        var previousBox: Int? = null
        val merged = dao.recordAnswer(profileId, verbId, tense.name, person.name) { existing ->
            previousBox = existing?.box
            val outcome = AtelierSchedule.answered(box = existing?.box ?: 0, correct = correct)
            AtelierReviewEntity(
                id = existing?.id ?: UUID.randomUUID().toString(),
                profileId = profileId,
                verbId = verbId,
                tense = tense.name,
                person = person.name,
                box = outcome.box,
                dueAt = nowMillis + outcome.nextDelay.inWholeMilliseconds,
                lapses = (existing?.lapses ?: 0) + if (correct) 0 else 1,
                updatedAt = nowMillis,
            )
        }
        val review = checkNotNull(merged.toDomainOrNull()) { "freshly written review row must map" }
        return AtelierAnswerOutcome(previousBox = previousBox, review = review)
    }

    override suspend fun sync() { /* no-op: cloud migration hook */ }
}
