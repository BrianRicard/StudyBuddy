package com.studybuddy.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favourite_poems")
data class FavouritePoemEntity(
    @PrimaryKey val id: String,
    val poemId: String,
    val poemSource: String,
    val profileId: String,
    val favouritedAt: Long,
)
