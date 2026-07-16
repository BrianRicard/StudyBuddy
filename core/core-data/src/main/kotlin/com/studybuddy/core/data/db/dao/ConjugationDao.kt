package com.studybuddy.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.studybuddy.core.data.db.entity.ConjugationProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConjugationDao {

    @Query("SELECT * FROM conjugation_progress WHERE profileId = :profileId")
    fun getProgressForProfile(profileId: String): Flow<List<ConjugationProgressEntity>>

    @Query(
        """
        SELECT * FROM conjugation_progress
        WHERE profileId = :profileId AND stageId = :stageId AND step = :step
        """,
    )
    suspend fun getStepProgress(
        profileId: String,
        stageId: String,
        step: String,
    ): ConjugationProgressEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(progress: ConjugationProgressEntity)

    @Update
    suspend fun update(progress: ConjugationProgressEntity)
}
