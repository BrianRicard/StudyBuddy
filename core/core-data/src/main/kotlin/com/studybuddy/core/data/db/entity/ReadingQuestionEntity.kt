package com.studybuddy.core.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reading_questions",
    foreignKeys = [
        ForeignKey(
            entity = ReadingPassageEntity::class,
            parentColumns = ["id"],
            childColumns = ["passageId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("passageId")],
)
data class ReadingQuestionEntity(
    @PrimaryKey val id: String,
    val passageId: String,
    val questionIndex: Int,
    val type: String,
    val questionText: String,
    val options: String?,
    val correctAnswer: String,
    val explanation: String,
    val evidenceSentenceIndex: Int,
)
