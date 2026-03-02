package com.studybuddy.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reading_sessions")
data class ReadingSessionEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val poemId: String,
    val score: Float,
    val accuracyPct: Float,
    val durationSeconds: Int,
    val language: String,
    val createdAt: Long,
)
