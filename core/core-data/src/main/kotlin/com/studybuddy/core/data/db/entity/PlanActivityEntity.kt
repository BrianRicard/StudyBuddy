package com.studybuddy.core.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One completed session, written once when the child reaches a mode's results.
 *
 * Separate from `point_events` on purpose: several drills award points per card, so
 * the points ledger cannot answer "how many Dictée *sessions* today".
 */
@Entity(
    tableName = "plan_activity",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("profileId", "completedAt")],
)
data class PlanActivityEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val mode: String,
    val completedAt: Long,
)
