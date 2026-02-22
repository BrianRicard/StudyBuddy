package com.studybuddy.core.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dictee_words",
    foreignKeys = [
        ForeignKey(
            entity = DicteeListEntity::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("listId")],
)
data class DicteeWordEntity(
    @PrimaryKey val id: String,
    val listId: String,
    val word: String,
    val mastered: Boolean = false,
    val attempts: Int = 0,
    val correctCount: Int = 0,
    val lastAttemptAt: Long? = null,
)
