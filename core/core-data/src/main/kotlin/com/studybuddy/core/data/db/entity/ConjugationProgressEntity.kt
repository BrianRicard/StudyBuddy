package com.studybuddy.core.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Best result for one step of one conjugation stage.
 * One row per (profileId, stageId, step), enforced by the unique index.
 */
@Entity(
    tableName = "conjugation_progress",
    indices = [Index("profileId", "stageId", "step", unique = true)],
)
data class ConjugationProgressEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val stageId: String,
    val step: String,
    val bestCorrect: Int,
    val bestTotal: Int,
    val completedAt: Long?,
    val updatedAt: Long,
)
