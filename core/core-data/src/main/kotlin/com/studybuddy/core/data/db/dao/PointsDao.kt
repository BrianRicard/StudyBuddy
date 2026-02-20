package com.studybuddy.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.studybuddy.core.data.db.entity.PointEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PointsDao {
    @Query("SELECT * FROM point_events WHERE profileId = :profileId ORDER BY timestamp DESC")
    fun getPointsForProfile(profileId: String): Flow<List<PointEventEntity>>

    @Query("SELECT COALESCE(SUM(points), 0) FROM point_events WHERE profileId = :profileId")
    fun getTotalPoints(profileId: String): Flow<Long>

    @Query(
        "SELECT COUNT(*) FROM point_events WHERE profileId = :profileId " +
            "AND timestamp >= :startOfDayMs"
    )
    fun getPointsToday(profileId: String, startOfDayMs: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: PointEventEntity)
}
