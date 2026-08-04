package com.studybuddy.core.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** One line of the parent's weekly plan. [dayOfWeek] is ISO — Monday is 1. */
@Entity(
    tableName = "plan_tasks",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("profileId", "dayOfWeek")],
)
data class PlanTaskEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val dayOfWeek: Int,
    val mode: String,
    val targetCount: Int,
    val updatedAt: Long,
)
