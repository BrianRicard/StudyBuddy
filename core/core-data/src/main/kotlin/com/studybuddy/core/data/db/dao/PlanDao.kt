package com.studybuddy.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.studybuddy.core.data.db.entity.PlanActivityEntity
import com.studybuddy.core.data.db.entity.PlanTaskEntity
import kotlinx.coroutines.flow.Flow

/** How many sessions of one mode fell inside a date range. */
data class ModeSessionCount(
    val mode: String,
    val sessions: Int,
)

@Dao
interface PlanDao {

    @Query("SELECT * FROM plan_tasks WHERE profileId = :profileId ORDER BY dayOfWeek, mode")
    fun getPlan(profileId: String): Flow<List<PlanTaskEntity>>

    @Query("SELECT * FROM plan_tasks WHERE profileId = :profileId AND dayOfWeek = :dayOfWeek ORDER BY mode")
    fun getPlanForDay(
        profileId: String,
        dayOfWeek: Int,
    ): Flow<List<PlanTaskEntity>>

    /** Authoritative single-row read, for resolving an increment against stored truth. */
    @Query("SELECT * FROM plan_tasks WHERE id = :taskId")
    suspend fun findTask(taskId: String): PlanTaskEntity?

    @Upsert
    suspend fun upsertTask(task: PlanTaskEntity)

    @Query("DELETE FROM plan_tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: PlanActivityEntity)

    @Query(
        "SELECT mode, COUNT(*) AS sessions FROM plan_activity " +
            "WHERE profileId = :profileId AND completedAt >= :fromMs AND completedAt < :toMs " +
            "GROUP BY mode",
    )
    fun getSessionCounts(
        profileId: String,
        fromMs: Long,
        toMs: Long,
    ): Flow<List<ModeSessionCount>>
}
