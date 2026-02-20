package com.studybuddy.core.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "point_events",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("profileId")],
)
data class PointEventEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val source: String,
    val points: Int,
    val reason: String,
    val timestamp: Long,
)
