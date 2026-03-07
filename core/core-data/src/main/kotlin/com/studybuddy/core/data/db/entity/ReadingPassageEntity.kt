package com.studybuddy.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reading_passages")
data class ReadingPassageEntity(
    @PrimaryKey val id: String,
    val language: String,
    val tier: Int,
    val theme: String,
    val title: String,
    val passage: String,
    val wordCount: Int,
    val source: String,
    val sourceAttribution: String?,
)
