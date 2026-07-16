package com.studybuddy.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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

    /**
     * Serialized read-merge-write for one step record. Room runs this on the
     * single write connection, so concurrent calls (e.g. a double-tapped
     * finish button) cannot race the unique (profileId, stageId, step) index.
     *
     * @param merge Maps the existing row (null on first run) to the row to
     * store; returning the existing instance unchanged skips the write.
     * @return true when a new row was inserted (first completion).
     */
    @Transaction
    suspend fun recordResult(
        profileId: String,
        stageId: String,
        step: String,
        merge: (ConjugationProgressEntity?) -> ConjugationProgressEntity,
    ): Boolean {
        val existing = getStepProgress(profileId, stageId, step)
        val merged = merge(existing)
        when {
            existing == null -> insert(merged)
            merged !== existing -> update(merged)
        }
        return existing == null
    }
}
