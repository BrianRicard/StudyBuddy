package com.studybuddy.core.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Leitner review state for one Jardin des Tables card.
 * One row per (profileId, tableNumber, multiplicand), enforced by the unique
 * index; rows are created the first time a fact is answered.
 *
 * The column is `tableNumber` (not `table`) because TABLE is an SQL keyword.
 */
@Entity(
    tableName = "math_facts_review",
    indices = [
        Index("profileId", "tableNumber", "multiplicand", unique = true),
        Index("profileId", "dueAt"),
    ],
)
data class MathFactReviewEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val tableNumber: Int,
    val multiplicand: Int,
    val box: Int,
    val dueAt: Long,
    val lapses: Int,
    val updatedAt: Long,
)
