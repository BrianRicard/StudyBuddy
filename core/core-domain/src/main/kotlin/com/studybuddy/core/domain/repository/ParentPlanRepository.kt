package com.studybuddy.core.domain.repository

import com.studybuddy.core.domain.model.LearningMode
import com.studybuddy.core.domain.model.PlanTask
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

/**
 * The parent's weekly plan, and the record of what the child actually did.
 *
 * Activity is stored explicitly rather than inferred from [PointEvent]s: several
 * drills award points per *card*, so counting point events would read one tables
 * session as fifteen.
 */
interface ParentPlanRepository {

    /** Every task across all seven days, for the parent's setup screen. */
    fun getPlan(profileId: String): Flow<List<PlanTask>>

    /** Tasks for one ISO weekday (Monday = 1). */
    fun getPlanForDay(
        profileId: String,
        dayOfWeek: Int,
    ): Flow<List<PlanTask>>

    /** Reads one task by id, bypassing the observed flow, which lags a write. */
    suspend fun findTask(taskId: String): PlanTask?

    suspend fun upsertTask(task: PlanTask)

    suspend fun deleteTask(taskId: String)

    /** Records that the child finished one session of [mode]. */
    suspend fun recordSession(
        profileId: String,
        mode: LearningMode,
        completedAt: Instant,
    )

    /** How many sessions of each mode were completed in the half-open range [from, to). */
    fun getSessionCounts(
        profileId: String,
        from: Instant,
        to: Instant,
    ): Flow<Map<LearningMode, Int>>

    /** Cloud migration hook — currently a no-op. Do not remove. */
    suspend fun sync()
}
