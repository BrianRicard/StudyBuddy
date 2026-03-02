package com.studybuddy.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_poems")
data class CachedPoemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val lines: String,
    val language: String,
    val source: String,
    val tags: String,
    val cachedAt: Long,
)
