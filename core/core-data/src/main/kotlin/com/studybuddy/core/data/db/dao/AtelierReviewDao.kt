package com.studybuddy.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.studybuddy.core.data.db.entity.AtelierReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AtelierReviewDao {

    @Query("SELECT * FROM atelier_review WHERE profileId = :profileId")
    fun getReviewsForProfile(profileId: String): Flow<List<AtelierReviewEntity>>

    @Query(
        """
        SELECT * FROM atelier_review
        WHERE profileId = :profileId AND verbId = :verbId AND tense = :tense AND person = :person
        """,
    )
    suspend fun getReview(
        profileId: String,
        verbId: String,
        tense: String,
        person: String,
    ): AtelierReviewEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(review: AtelierReviewEntity)

    @Update
    suspend fun update(review: AtelierReviewEntity)

    /**
     * Serialized read-merge-write for one card. Room runs this on the single
     * write connection, so concurrent calls (e.g. a double-tapped submit)
     * cannot race the unique (profileId, verbId, tense, person) index.
     *
     * @param merge Maps the existing row (null on first sighting) to the row
     * to store.
     * @return The stored row.
     */
    @Transaction
    suspend fun recordAnswer(
        profileId: String,
        verbId: String,
        tense: String,
        person: String,
        merge: (AtelierReviewEntity?) -> AtelierReviewEntity,
    ): AtelierReviewEntity {
        val existing = getReview(profileId, verbId, tense, person)
        val merged = merge(existing)
        if (existing == null) insert(merged) else update(merged)
        return merged
    }
}
