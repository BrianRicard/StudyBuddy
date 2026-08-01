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
    /**
     * When this card first reached the top box, or null if it never has.
     * A high-water mark: it is set once and never cleared, so a later lapse
     * cannot rewrite history for the parent-facing milestones.
     */
    val masteredAt: Long? = null,
)
