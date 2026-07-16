package com.studybuddy.core.data.repository

import com.studybuddy.core.data.db.dao.ConjugationDao
import com.studybuddy.core.data.db.entity.ConjugationProgressEntity
import com.studybuddy.core.data.mapper.toDomain
import com.studybuddy.core.domain.model.conjugation.ConjugationProgress
import com.studybuddy.core.domain.model.conjugation.ConjugationStep
import com.studybuddy.core.domain.repository.ConjugationRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

@Singleton
class LocalConjugationRepository @Inject constructor(
    private val dao: ConjugationDao,
) : ConjugationRepository {

    override fun getProgressForProfile(profileId: String): Flow<List<ConjugationProgress>> =
        dao.getProgressForProfile(profileId).map { rows -> rows.map { it.toDomain() } }

    override suspend fun recordStepResult(
        profileId: String,
        stageId: String,
        step: ConjugationStep,
        correct: Int,
        total: Int,
    ) {
        val now = Clock.System.now().toEpochMilliseconds()
        val existing = dao.getStepProgress(profileId, stageId, step.name)
        if (existing == null) {
            dao.insert(
                ConjugationProgressEntity(
                    id = UUID.randomUUID().toString(),
                    profileId = profileId,
                    stageId = stageId,
                    step = step.name,
                    bestCorrect = correct,
                    bestTotal = total,
                    completedAt = now,
                    updatedAt = now,
                ),
            )
        } else {
            val isBetter = correct * existing.bestTotal > existing.bestCorrect * total ||
                existing.bestTotal == 0
            dao.update(
                existing.copy(
                    bestCorrect = if (isBetter) correct else existing.bestCorrect,
                    bestTotal = if (isBetter) total else existing.bestTotal,
                    completedAt = existing.completedAt ?: now,
                    updatedAt = now,
                ),
            )
        }
    }

    override suspend fun sync() { /* no-op: cloud migration hook */ }
}
