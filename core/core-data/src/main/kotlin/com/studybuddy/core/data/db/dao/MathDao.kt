package com.studybuddy.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.studybuddy.core.data.db.entity.MathSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MathDao {
    @Query("SELECT * FROM math_sessions WHERE profileId = :profileId ORDER BY completedAt DESC")
    fun getSessionsForProfile(profileId: String): Flow<List<MathSessionEntity>>

    @Query("SELECT * FROM math_sessions WHERE id = :sessionId")
    fun getSession(sessionId: String): Flow<MathSessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: MathSessionEntity)

    @Query("DELETE FROM math_sessions WHERE id = :sessionId")
    suspend fun delete(sessionId: String)
}
