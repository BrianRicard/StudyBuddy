package com.studybuddy.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.studybuddy.core.data.db.entity.MathFactReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MathFactsReviewDao {

    @Query("SELECT * FROM math_facts_review WHERE profileId = :profileId")
    fun getReviewsForProfile(profileId: String): Flow<List<MathFactReviewEntity>>

    /** Every row, for backup export. */
    @Query("SELECT * FROM math_facts_review")
    suspend fun getAllReviews(): List<MathFactReviewEntity>

    @Query(
        """
        SELECT * FROM math_facts_review
        WHERE profileId = :profileId AND tableNumber = :tableNumber AND multiplicand = :multiplicand
        """,
    )
    suspend fun getReview(
        profileId: String,
        tableNumber: Int,
        multiplicand: Int,
    ): MathFactReviewEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(review: MathFactReviewEntity)

    @Update
    suspend fun update(review: MathFactReviewEntity)

    /**
     * Serialized read-merge-write for one card. Room runs this on the single
     * write connection, so concurrent calls (e.g. a double-tapped submit)
     * cannot race the unique (profileId, tableNumber, multiplicand) index.
     *
     * @param merge Maps the existing row (null on first sighting) to the row
     * to store.
     * @return The stored row.
     */
    @Transaction
    suspend fun recordAnswer(
        profileId: String,
        tableNumber: Int,
        multiplicand: Int,
        merge: (MathFactReviewEntity?) -> MathFactReviewEntity,
    ): MathFactReviewEntity {
        val existing = getReview(profileId, tableNumber, multiplicand)
        val merged = merge(existing)
        if (existing == null) insert(merged) else update(merged)
        return merged
    }
}
