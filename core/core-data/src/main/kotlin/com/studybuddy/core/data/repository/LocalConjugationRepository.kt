package com.studybuddy.core.data.repository

import com.studybuddy.core.data.db.dao.ConjugationDao
import com.studybuddy.core.data.db.entity.ConjugationProgressEntity
import com.studybuddy.core.data.mapper.toDomainOrNull
import com.studybuddy.core.domain.model.conjugation.ConjugationProgress
import com.studybuddy.core.domain.model.conjugation.ConjugationStep
import com.studybuddy.core.domain.repository.ConjugationRepository
import com.studybuddy.core.domain.repository.StepResultOutcome
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
        dao.getProgressForProfile(profileId).map { rows -> rows.mapNotNull { it.toDomainOrNull() } }

    override suspend fun recordStepResult(
        profileId: String,
        stageId: String,
        step: ConjugationStep,
        correct: Int,
        total: Int,
    ): StepResultOutcome {
        require(total >= 0 && correct in 0..total) {
            "Invalid step result: correct=$correct, total=$total"
        }
        val now = Clock.System.now().toEpochMilliseconds()
        var newBest = false
        val inserted = dao.recordResult(profileId, stageId, step.name) { existing ->
            when {
                existing == null -> {
                    newBest = true
                    ConjugationProgressEntity(
                        id = UUID.randomUUID().toString(),
                        profileId = profileId,
                        stageId = stageId,
                        step = step.name,
                        bestCorrect = correct,
                        bestTotal = total,
                        completedAt = now,
                        updatedAt = now,
                    )
                }

                isBetter(correct, total, existing) -> {
                    newBest = true
                    existing.copy(
                        bestCorrect = correct,
                        bestTotal = total,
                        completedAt = existing.completedAt ?: now,
                        updatedAt = now,
                    )
                }

                // Worse or equal run: keep the stored best untouched so
                // updatedAt keeps meaning "when the best last improved".
                else -> existing
            }
        }
        return StepResultOutcome(firstCompletion = inserted, newBest = newBest)
    }

    /** Higher correct/total ratio wins; ties broken by the larger total. */
    private fun isBetter(
        correct: Int,
        total: Int,
        existing: ConjugationProgressEntity,
    ): Boolean = when {
        existing.bestTotal == 0 -> total > 0
        correct * existing.bestTotal != existing.bestCorrect * total ->
            correct * existing.bestTotal > existing.bestCorrect * total

        else -> total > existing.bestTotal
    }

    override suspend fun sync() { /* no-op: cloud migration hook */ }
}
