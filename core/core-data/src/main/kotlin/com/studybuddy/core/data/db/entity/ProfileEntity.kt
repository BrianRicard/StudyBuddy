package com.studybuddy.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val locale: String = "en",
    val totalPoints: Long = 0,
    val bodyId: String = "fox",
    val hatId: String = "none",
    val faceId: String = "none",
    val outfitId: String = "default",
    val petId: String = "none",
    val equippedTitle: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)
