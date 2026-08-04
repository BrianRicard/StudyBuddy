package com.studybuddy.core.data.repository

import com.studybuddy.core.data.db.dao.PlanDao
import com.studybuddy.core.data.db.entity.PlanActivityEntity
import com.studybuddy.core.data.db.entity.PlanTaskEntity
import com.studybuddy.core.domain.model.LearningMode
import com.studybuddy.core.domain.model.PlanTask
import com.studybuddy.core.domain.repository.ParentPlanRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

@Singleton
class LocalParentPlanRepository @Inject constructor(private val dao: PlanDao) : ParentPlanRepository {

    override fun getPlan(profileId: String): Flow<List<PlanTask>> =
        dao.getPlan(profileId).map { tasks -> tasks.mapNotNull { it.toDomain() } }

    override fun getPlanForDay(
        profileId: String,
        dayOfWeek: Int,
    ): Flow<List<PlanTask>> =
        dao.getPlanForDay(profileId, dayOfWeek).map { tasks -> tasks.mapNotNull { it.toDomain() } }

    override suspend fun findTask(taskId: String): PlanTask? = dao.findTask(taskId)?.toDomain()

    override suspend fun upsertTask(task: PlanTask) = dao.upsertTask(task.toEntity())

    override suspend fun deleteTask(taskId: String) = dao.deleteTask(taskId)

    override suspend fun recordSession(
        profileId: String,
        mode: LearningMode,
        completedAt: Instant,
    ) = dao.insertActivity(
        PlanActivityEntity(
            id = UUID.randomUUID().toString(),
            profileId = profileId,
            mode = mode.name,
            completedAt = completedAt.toEpochMilliseconds(),
        ),
    )

    override fun getSessionCounts(
        profileId: String,
        from: Instant,
        to: Instant,
    ): Flow<Map<LearningMode, Int>> = dao.getSessionCounts(
        profileId = profileId,
        fromMs = from.toEpochMilliseconds(),
        toMs = to.toEpochMilliseconds(),
    ).map { rows ->
        rows.mapNotNull { row -> row.mode.toModeOrNull()?.let { it to row.sessions } }.toMap()
    }

    override suspend fun sync() = Unit
}

/**
 * Rows whose mode no longer exists are dropped rather than crashing: a downgrade, or
 * a mode removed in a later version, must not take the parent's whole plan with it.
 */
private fun String.toModeOrNull(): LearningMode? = LearningMode.entries.firstOrNull { it.name == this }

private fun PlanTaskEntity.toDomain(): PlanTask? {
    val mode = mode.toModeOrNull() ?: return null
    return PlanTask(
        id = id,
        profileId = profileId,
        dayOfWeek = dayOfWeek,
        mode = mode,
        targetCount = targetCount,
        updatedAt = Instant.fromEpochMilliseconds(updatedAt),
    )
}

private fun PlanTask.toEntity() = PlanTaskEntity(
    id = id,
    profileId = profileId,
    dayOfWeek = dayOfWeek,
    mode = mode.name,
    targetCount = targetCount,
    updatedAt = updatedAt.toEpochMilliseconds(),
)
