package com.studybuddy.core.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Leitner review state for one Atelier drill card.
 * One row per (profileId, verbId, tense, person), enforced by the unique
 * index; rows are created the first time a card is answered.
 */
@Entity(
    tableName = "atelier_review",
    indices = [
        Index("profileId", "verbId", "tense", "person", unique = true),
        Index("profileId", "dueAt"),
    ],
)
data class AtelierReviewEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val verbId: String,
    val tense: String,
    val person: String,
    val box: Int,
    val dueAt: Long,
    val lapses: Int,
    val updatedAt: Long,
)
