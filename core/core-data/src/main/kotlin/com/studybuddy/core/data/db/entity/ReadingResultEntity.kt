package com.studybuddy.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reading_results")
data class ReadingResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val passageId: String,
    val score: Int,
    val totalQuestions: Int,
    val pointsEarned: Int,
    val readingTimeMs: Long,
    val questionsTimeMs: Long,
    val completedAt: Long,
    val allCorrectFirstTry: Boolean,
)
