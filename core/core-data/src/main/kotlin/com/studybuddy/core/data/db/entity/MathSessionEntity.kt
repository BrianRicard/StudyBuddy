package com.studybuddy.core.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "math_sessions",
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
data class MathSessionEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val operators: String,
    val rangeMin: Int,
    val rangeMax: Int,
    val totalProblems: Int,
    val correctCount: Int,
    val bestStreak: Int,
    val avgResponseMs: Long,
    val difficulty: String,
    val completedAt: Long,
)
